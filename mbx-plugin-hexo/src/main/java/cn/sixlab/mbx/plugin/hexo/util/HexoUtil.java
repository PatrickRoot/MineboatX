/*
 * Copyright (c) 2018 Sixlab. All rights reserved.
 * <p>
 * License information see the LICENSE file in the project's root directory.
 * <p>
 * For more information, please see
 * https://sixlab.cn/
 *
 * @time: 2018/5/20 23:46
 * @author: Patrick <root@sixlab.cn>
 */
package cn.sixlab.mbx.plugin.hexo.util;

import cn.sixlab.mbx.core.common.util.Encoding;
import cn.sixlab.mbx.plugin.hexo.bean.HexoArticle;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class HexoUtil {
    private static String hexoPath;
    private static String hexoPostPath;
    
    @Value("${mbx.hexo.path}")
    public void setHexoPath(String hexoPath) {
        this.hexoPath = hexoPath;
        this.hexoPostPath = hexoPath + "/source/_posts";
    }
    
    public static String postPath() {
        return hexoPostPath;
    }
    
    public static String imgPath() {
        return hexoPath+"/source/images";
    }
    
    public static List<HexoArticle> getArticles() {
        if (notChange()) {
            return HexoCache.articles;
        }
        List<HexoArticle> articles = new ArrayList<>();
        
        File postFolder = new File(hexoPostPath);
        File[] files = postFolder.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".md"));
        
        for (File file : files) {
            HexoArticle article = getArticle(file);
            if (null != article) {
                articles.add(article);
            }
        }
        articles.sort((o1, o2) -> {
            String date1 = o1.getDate().replaceAll("\\ |\\-|\\:","");
            String date2 = o2.getDate().replaceAll("\\ |\\-|\\:", "");
            long d1 = Long.parseLong(date1);
            long d2 = Long.parseLong(date2);
            return (d1 - d2) > 0 ? -1 : 1;
        });
        
        HexoCache.articles = articles;
        return articles;
    }
    
    public static boolean saveArticle(String fileId, String content) {
        try {
            File file = new File(hexoPostPath + File.separator + fileId + ".md");
            if (!file.exists()) {
                file.createNewFile();
            }
    
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(content);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static HexoArticle getArticle(String filename) {
        return getArticle(new File(hexoPostPath + File.separator + filename+".md"));
    }
    
    private static HexoArticle getArticle(File file) {
        if (null == file || !file.isFile()) {
            return null;
        }
        
        String filename = file.getName();
        
        if (!filename.endsWith(".md")) {
            return null;
        }
        
        try {
            HexoArticle article = new HexoArticle();
            
            String content = FileUtils.readFileToString(file, Encoding.UTF8);
            article.setContent(content);
            
            filename = filename.substring(0, filename.lastIndexOf("."));
            article.setFileId(filename);
            
            int titleStart = content.indexOf("title:") + 7;
            int titleEnd = content.indexOf("\n", titleStart);
            String title = content.substring(titleStart, titleEnd);
            article.setTitle(title);
            
            int idStart = content.indexOf("id:") + 4;
            int idEnd = content.indexOf("\n", idStart);
            String id = content.substring(idStart, idEnd);
            article.setArchiveId(id);
            
            int dateStart = content.indexOf("date:") + 6;
            int dateEnd = content.indexOf("\n", dateStart);
            String date = content.substring(dateStart, dateEnd);
            article.setDate(date);
            
            return article;
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static boolean notChange() {
        File file = new File(hexoPostPath);
        long lastModified = file.lastModified();
        if (lastModified == HexoCache.lastModified) {
            return true;
        }
        HexoCache.lastModified = lastModified;
        return false;
    }
    
    public static boolean deleteArticle(String fileId) {
        File file = new File(hexoPostPath + File.separator + fileId + ".md");
        if (file.exists()) {
            file.delete();
        }
    
        return true;
    }
}
