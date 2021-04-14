package com.leyou.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    private static final ObjectMapper MAPPER = new ObjectMapper();



    @Autowired
    private GoodsRepository goodsRepository;

    public Goods buildGoods(Spu spu) throws JsonProcessingException {
        Goods goods = new Goods();

        //根据分类id查询分类名称
        List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //根据品牌id查询品牌名称
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //根据SPU的id查询所有的SKU
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        //初始化价格集合，搜集所有SKU的价格
        List<Long> prices = new ArrayList<>();
        //搜集SKU的字段信息
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            //获取SKU中的图片，数据库中图片可能是多张，多张以，进行分割，所以也以，来进行切割，返回图片数组，获取第一张图片
            map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            skuMapList.add(map);
        });
        //根据SPU中的cid3查询出所有的搜索规格参数
        List<SpecParam> specParams = this.specificationClient.queryParams(null, spu.getCid3(), null, true);
        //根据spuid查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spu.getId());
        //把通用的规格参数值进行反序列化
        Map<String, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {
        });
        //把特殊的规格参数值进行反序列化
        Map<String, List<Object>> specialSepcMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>() {
        });
        Map<String, Object> specs = new HashMap<>();
        specParams.forEach(param -> {
            //判定规格参数的类型是否是通用的规格参数
            if (param.getGeneric()) {
                //如果是通用类型的参数从gen获取规格参数值
                String value = genericSpecMap.get(param.getId().toString()).toString();
                //判定是否是数值类型，如果是数值类型，应该返回一个区间
                if (param.getNumeric()) {
                    value = chooseSegment(value, param);
                }
                specs.put(param.getName(), value);
            } else {
                //如果是特殊的规格参数从spec中获取值
                List<Object> value = specialSepcMap.get(param.getId().toString());
                specs.put(param.getName(), value);
            }
        });

        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        //拼接all字段，需要分类名称以及品牌名称
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " ") + " " + brand.getName());
        //获取spu下的所有sku的价格
        goods.setPrice(prices);
        //获取spu下的所有SKU并转换成json字符串
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        //获取所有的查询的规格参数{name:value}
        goods.setSpecs(specs);

        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest searchRequest) {
        if (StringUtils.isBlank(searchRequest.getKey())) {
            return null;
        }
        //自定义查询构建器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //添加查询添加
        //QueryBuilder baseQuery = QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND);
        BoolQueryBuilder baseQuery=buildBoolQueryBuild(searchRequest);
        nativeSearchQueryBuilder.withQuery(baseQuery);
        //添加分页
        nativeSearchQueryBuilder.withPageable(PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize()));
        //添加结果集过滤
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));
        //添加分类和品牌的聚合
        String categoryAggName="categories";
        String brandAggName="brands";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //执行查询，获取结果集
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(nativeSearchQueryBuilder.build());
        //获取聚合结果集并解析
        List<Map<String,Object>> categories=getCategoryAggResult(goodsPage.getAggregation(categoryAggName));
        List<Brand> brands=getBrandAggResult(goodsPage.getAggregation(brandAggName));
        //判断，是否是一个分类，只有一个分类时做规格参数聚合
        List<Map<String,Object>> paramAggResult=null;
        if(!CollectionUtils.isEmpty(categories) && categories.size()==1){
            //对规格参数进行聚合
            paramAggResult=getParamAggResult((Long)categories.get(0).get("id"),baseQuery);
        }
        return new SearchResult(goodsPage.getTotalElements(),goodsPage.getTotalPages(),goodsPage.getContent(),categories,brands,paramAggResult);
    }

    //构建bool查询
    private BoolQueryBuilder buildBoolQueryBuild(SearchRequest searchRequest) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //添加基本查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));
        //添加过滤条件
        //获取用户选额的过滤信息
        Map<String, Object> filter = searchRequest.getFilter();
        for (Map.Entry<String,Object> entry:filter.entrySet()){
            String key = entry.getKey();
            if(StringUtils.equals("品牌", key)){
                key="brandId";
            }else if (StringUtils.equals("分类", key)){
                key="cid3";
            }else {
                key="specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }
        return boolQueryBuilder;
    }

    //根据查询条件聚合规格参数
    private List<Map<String,Object>> getParamAggResult(Long cid, QueryBuilder baseQuery) {
        //自定义查询对象构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本查询条件
        queryBuilder.withQuery(baseQuery);
        //查询要聚合的规格参数
        List<SpecParam> specParams = this.specificationClient.queryParams(null, cid, null, true);
        //添加规格参数聚合
        specParams.forEach(param -> {
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName() +".keyword"));
        });
        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));
        //执行聚合查询,获取聚合结果集
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());
        List<Map<String,Object>> spec=new ArrayList<>();
        //解析聚合结果集,key-聚合名称
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for (Map.Entry<String,Aggregation> entry:aggregationMap.entrySet()){
            //初始化一个Map
            Map<String,Object> map=new HashMap<>();
            map.put("k", entry.getKey());
            //初始化一个options，收集桶中的key
            List<String> options=new ArrayList<>();
            StringTerms terms = (StringTerms)entry.getValue();
            terms.getBuckets().forEach(bucket -> {
                options.add(bucket.getKeyAsString());
            });
            map.put("options", options);
            spec.add(map);
        }
        return spec;
    }

    //解析品牌的聚合结果集
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        LongTerms longTerms=(LongTerms)aggregation;

        //获取集合桶
        return longTerms.getBuckets().stream().map(bucket -> {
            return this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
        }).collect(Collectors.toList());
    }

    //解析分类的聚合结果集
    private List<Map<String,Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms longTerms=(LongTerms)aggregation;

        //获取桶的集合，转换成List<Map<String,Object>>
        return longTerms.getBuckets().stream().map(bucket -> {
            //初始化map
            Map<String,Object> map=new HashMap<>();
            Long id=bucket.getKeyAsNumber().longValue();
            //根据分类id查询分类名称
            List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(id));
            map.put("id", id);
            map.put("name", names.get(0));
            System.out.println ("id:"+id+" name:"+names.get (0));
            return map;
        }).collect(Collectors.toList());
    }


    public void save(Long id) throws JsonProcessingException {
        Spu spu = this.goodsClient.querySpuById (id);
        Goods goods = this.buildGoods (spu);
        this.goodsRepository.save (goods);
    }

    public void delete(Long id){
        this.goodsRepository.deleteById (id);
    }
}