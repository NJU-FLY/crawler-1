package spider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import pojo.CnkiResult;
import pojo.GTResult;
import pojo.SearchResult;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/9/5.
 */
public class DangdangSpider {
    String cookieStr;
    private Util util = new Util();

    //当当查询
    public Element queryDangdang(SearchResult searchResult) throws IOException {
        System.out.println("--------------开始------------------");
        System.out.println("书名：" + searchResult.getTitle() + "   作者：" + searchResult.getAuthor() + "    出版社：" + searchResult.getPublisher());
        Integer result = null;
        int ddSale = 0;
        //去空格
        String title = searchResult.getTitle();
        String author = searchResult.getAuthor();

        //引号改英文
        title = util.formatTitleString(title);
        //格式化查询条件
        String[] parsedTitleAuthor = util.formatSearchResult(searchResult);
        String searchTitle = parsedTitleAuthor[0];
        String searchAuthor = parsedTitleAuthor[1];
        String searchSpareTitle = parsedTitleAuthor[2];
        String searchSpareAuthor = parsedTitleAuthor[3];
        //备用书名生成
        searchSpareTitle = util.createSpareTile(searchTitle, searchSpareTitle);

        String url = "http://search.dangdang.com/?key=" + URLEncoder.encode(searchTitle, "utf-8") + "+" + URLEncoder.encode(searchAuthor, "utf-8") + "&ddSale=1";
        System.out.println("url: " + url);
        String resText = getEntity(url);
        //        System.out.println(resText);
        Document doc = Jsoup.parse(new URL(url).openStream(), "gbk", url);
        if (doc.select("p[class=dang]").size() <= 0) {
            System.out.println("---------没有当当自营-----------");
        }
        Elements titleNodes = doc.select("a[name=itemlist-title]");
        for (int i = 0; i < titleNodes.size(); i++) {
            //如果有当当自营，非自营的就跳过
            if (doc.select("p[class=dang]").size() > 0 && titleNodes.get(i).parent().parent().select("p[class=dang]").size() <= 0) {
                continue;
            }
            Element parentLi = titleNodes.get(i).parent().parent();
            Element authorNode = null;
            Element publisherNode = null;
            if (parentLi.select("a[name=itemlist-author]").size() > 0) {
                authorNode = parentLi.select("a[name=itemlist-author]").get(0);
            }
            if (parentLi.select("a[name=P_cbs]").size() > 0) {
                publisherNode = parentLi.select("a[name=P_cbs]").get(0);
            }
            String titleNodeStr = titleNodes.get(i).attr("title");
            titleNodeStr = util.formatTitleString(titleNodeStr);
            if (titleNodeStr.contains(searchTitle) && !titleNodeStr.contains("电子书")) {
                if (authorNode == null || authorNode.attr("title").contains(author) || author.contains(authorNode.attr("title"))) {
                    if (publisherNode == null || publisherNode.attr("title").contains(searchResult.getPublisher())) {
                        if (authorNode == null && publisherNode == null) {
                            System.out.println("失败，既没有作者信息，也没有出版社信息");
                        } else {
                            System.out.println("-------查询条件：作者+书名，书名匹配成功，作者或出版社至少一个匹配成功-------：");
                            return parentLi;
                        }
                    } else {
                        System.out.println("-------查询条件：作者+书名，出版社匹配失败-----------");
                    }
                } else {
                    System.out.println("-------查询条件：作者+书名，作者匹配失败-----------");
                }
            }
        }
//        String url1 = "http://search.dangdang.com/?key=" + URLEncoder.encode(searchTitle, "utf-8") + "&ddSale=1";
//        System.out.println("url: " + url1);
//        resText = getEntity(url1);
//        Document noOfficeSaleDoc = Jsoup.parse(new URL(url1).openStream(), "gbk", url);
//        titleNodes = noOfficeSaleDoc.select("a[name=itemlist-title]");
//        for (int i = 0; i < titleNodes.size(); i++) {
//            Element parentLi = titleNodes.get(i).parent().parent();
//            Element authorNode = null;
//            if (parentLi.select("a[name=itemlist-author]").size() > 0) {
//                authorNode = parentLi.select("a[name=itemlist-author]").get(0);
//            }
//            String titleNodeStr = titleNodes.get(i).attr("title");
//            titleNodeStr = util.formatTitleString(titleNodeStr);
//            if (titleNodeStr.contains(searchTitle) && !titleNodeStr.contains("电子书")) {
//                if (authorNode == null || authorNode.attr("title").contains(author) || author.contains(authorNode.attr("title"))) {
//                    return parentLi;
//                }
//            }
//        }
//        System.out.println("------------查询条件：书名，书名匹配失败或作者匹配失败-----------");
        return null;
    }

    //返回评论数
    public Integer getCommentsByParams(SearchResult searchResult) throws Exception {
        Element parentLi = queryDangdang(searchResult);
        if (parentLi == null) {
            return null;
        } else {
            Element commentNode = parentLi.select("a[name=itemlist-review]").get(0);
            String commentStr = commentNode.text();
            System.out.println(commentStr);
            return Integer.parseInt(commentStr.substring(0, commentStr.indexOf("条")));
        }
    }

    //获取http响应
    public String getEntity(String url) throws IOException {
        CnkiResult cnkiResult = new CnkiResult();
        cookieStr = "__permanent_id=20150907220016537412413853055524459; __ddclick_visit=0000000001.1; out_refer=%7C; ddscreen=2; __trace_id=20150907221429847385123453431947745; __xsptplus100=100.1.1441634422.1441635269.11%234%7C%7C%7C%7C%7C%23%23lpvsf3w78GoYK7B34SayKvKAO4Ko3vDc%23; _jzqco=%7C%7C%7C%7C%7C1.1715151580.1441634422123.1441635202934.1441635269999.1441635202934.1441635269999.0.0.0.11.11; pos_9_end=1441635270056; ad_ids=1777500%2C1765433%2C1765387%7C%236%2C11%2C9; pos_0_start=1441635270176; pos_0_end=1441635270184";
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = null;
        HttpEntity entity = null;
        Map<String, String> headers = new HashMap<String, String>();
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36");
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httpGet.setHeader("Cookie", cookieStr);

        httpGet.setHeader("Host", "search.dangdang.com");
        httpGet.setHeader("Referer", "http://search.dangdang.com");
        response = httpClient.execute(httpGet);
        entity = response.getEntity();
        String resText = EntityUtils.toString(entity, "utf-8");
        return resText;
    }

    //返回url
    public String getBookInfoUrl(SearchResult searchResult) throws Exception {
        Element parentLi = queryDangdang(searchResult);
        if (parentLi == null) {
            return "";
        } else {
            Element titleNode = parentLi.select("p[name=title] a").get(0);
            String titleStr = titleNode.attr("href");
            return titleStr;
        }
    }

    //当当获取图书信息
    public GTResult getBookInfo(String url) throws Exception {
        GTResult gtResult = new GTResult();
        String resText = getEntity(url);
//        System.out.println(resText);
        //概述信息
        Document doc = Jsoup.parse(resText);
        //书名
        Elements title = doc.select("h1");
        for (Element h : title) {
            System.out.println("标题： " + h.text());
            gtResult.setTitle(h.text());
        }
        //作者，出版社，出版时间，isbn，分类
        Elements otherInfo = doc.select("ul[class=key clearfix] li");
        for (Element info : otherInfo) {
            if (info.text().contains("页 数")) {
                String str = info.text();
                str = str.substring(str.indexOf("：") + 1, str.length());
                System.out.println("页数： " + str);
                gtResult.setPage(str);
            }
            if (info.toString().contains("国际标准书号")) {
                String str = info.text();
                str = str.substring(str.indexOf("：") + 1, str.length());
                System.out.println("isbn： " + str);
                gtResult.setISBN("ISBN:"+str);
            }
            if (info.toString().contains("所属分类")) {
                String str = info.text();
                str = str.substring(str.indexOf("：") + 1, str.length());
                System.out.println("所属分类： " + str);
                gtResult.setLibrarySort(str);
            }
        }
        if (otherInfo.size() <= 0) {
            System.out.println("-------没有详细信息----");
        }
        //定价,未测试代码
        Element price = doc.select("p#dd-price").get(0);
        gtResult.setPrice(price.ownText());
        System.out.println("价格： "+ price.ownText());
        //语种要手动补充，比如那个藏文的语种不是汉语，但是译著的语种是汉语。
        //著作类型要手动补充，翻译的书都是译著。（国图的数据也是）
        //译著的主要责任人改成翻译者，主要负责人的格式去掉[]著等字符（国图的也是）
        Element author = doc.select("span#author").get(0);
        gtResult.setAllAuthor(author.text());
        System.out.println(author.text());
        return gtResult;
    }

    //单元测试
    public static void main(String[] args) throws Exception {
        DangdangSpider dangdangSpider = new spider.DangdangSpider();
        SearchResult searchResult = new SearchResult("文学文本解读学", "孙绍振", "北京大学出版社", 0, "");
        System.out.println(dangdangSpider.getCommentsByParams(searchResult));

    }
}
