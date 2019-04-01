package com.iask.seo.task;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iask.seo.bean.NoticeBean;
import com.iask.seo.bean.BaseHelper;
import com.wenwo.background.client.bean.RedisKey;
import com.wenwo.platform.entity.bg.SEOWordLibrary;
import com.wenwo.platform.utils.HttpClient;


/**
 * <p>SEO系统-跑PC排名</p>
 * 【定义多少个 @Scheduled ， 等于定义多少个线程】
 * 
 * @author flm
 *
 */
@Component
public class SeoRankPCTask {
	
	private final static Logger logger = LoggerFactory.getLogger(SeoRankPCTask.class);
	private static final long SECOND = 1000 ;
	
	@Value("${spring.http.url.pull}")
	private String URL_PULL;
	@Value("${spring.http.url.push}")
	private String URL_PUSH;
	@Value("${task.sleep.msecond}")
	private int TASK_SLEEP;
	
	
	// initialDelay 第一次延迟1秒执行，fixedDelay 在上一次执行完毕时间点2秒再次执行
    @Scheduled(initialDelay = SECOND * 1, fixedDelay = SECOND*2 )  
    public void runRankJob1() throws InterruptedException {
    	runRankPC("1");
    }
    
    @Scheduled(initialDelay = SECOND * 3, fixedDelay = SECOND*2)       
    public void runRankJob2() throws InterruptedException {
    	runRankPC("2");
    }
    
    @Scheduled(initialDelay = SECOND * 5, fixedDelay = SECOND*2)       
    public void runRankJob3() throws InterruptedException {
    	runRankPC("3");
    }
    
    @Scheduled(initialDelay = SECOND * 7, fixedDelay = SECOND*2 )       
    public void runRankJob4() throws InterruptedException {
    	runRankPC("4");
    }
    
    @Scheduled(initialDelay = SECOND * 10, fixedDelay = SECOND*2 )       
    public void runRankJob5() throws InterruptedException {
    	runRankPC("5");
    }
    
    
    
    
    
    // 价值得分 --多线程thread调用不能一样
 	private void runRankPC(String thread) throws InterruptedException {
 		logger.info("runRankPC  thread :" + thread);
 		try {
 			String key = RedisKey.SEO_WORD_RANK_PC;
 			
 			Map<String, Object> mapParam = new HashMap<String, Object>();
 			mapParam.put("key",key);
 			mapParam.put("thread",thread);
 			
 			String sr = BaseHelper.post( URL_PULL, mapParam );
 			Gson gson = new Gson();
 			NoticeBean noticeBean = gson.fromJson(sr, new TypeToken<NoticeBean>(){}.getType());
 			
 			if( !"0".equals(noticeBean.getCode()) || noticeBean.getObject() == null || noticeBean.getObject().size()<1 ){
 				logger.info("runRankPC no data ... :"+noticeBean.getCode() + "   , sleep ms:" +TASK_SLEEP);
 				Thread.sleep( TASK_SLEEP );					// 没有数据时 ，睡眠20分钟，防止过分请求后台压力大
 				return;
 			}
 			
 			List<SEOWordLibrary> libraries =gson.fromJson(gson.toJson(noticeBean.getObject()), new TypeToken<List<SEOWordLibrary>>(){}.getType());
 			
 			int i = 0;
 			for(SEOWordLibrary library : libraries){
 				i++;
 				logger.info("runRankPC  word  关键词Id:"+library.getId()+"  ....  总数："+libraries.size()+"   跑到第#i:"+i);
 				
 				// 跑排名
 				Map<String,Map<String,String>> map  = BaseHelper.getRank(library.getName());
 				Map<String,String> iask = map.get("iask");
 				Map<String,String> ishare = map.get("ishare");
 				
 				// iask
 				if(iask!=null){
 					String url = iask.get("url");
 					int pn =   Integer.valueOf(iask.get("pn"));
 					library.setIaskRank(pn);
 					library.setIaskUrl(url);
 				}
 				else{
 					library.setIaskRank(500);// 排名超出 50+
 				}
 				// ishare
 				if(ishare!=null){
 					String url = ishare.get("url");
 					int pn =  Integer.valueOf(ishare.get("pn"));
 					library.setIshareRank(pn);
 					library.setIshareUrl(url);
 				}else{
 					library.setIshareRank(500);
 				}
 				
 				// 获取  最佳 排名
 				String goodUrl = "";
 				int good = 500;
 				if(library.getIaskRank()>library.getIshareRank()){
 					good = library.getIshareRank();
 					goodUrl = library.getIshareUrl();
 				}
 				else{
 					good = library.getIaskRank();
 					goodUrl = library.getIaskUrl();
 				}
 				
 				if(good!=500 && goodUrl!=null){
 					library.setGoodRank(good);
 					library.setGoodUrl(goodUrl);
 				}else{
 					library.setGoodRank(500);
 				}
 			}
 			
 			// 返回数据给 后台
 			Map<String, Object> mapParam2 = new HashMap<String, Object>();
 			mapParam2.put("key",key);
 			mapParam2.put("thread",thread);
 			mapParam2.put("msg", gson.toJson(libraries));
 			String rt = BaseHelper.post(URL_PUSH, mapParam2);
 			
 			logger.info("runRankPC push rt:"+rt);
 			
 		} catch (Exception e) {
 			logger.error("runRankPC  run  error ..."+ "  sleep ms:" +TASK_SLEEP, e );
 			Thread.sleep( TASK_SLEEP);
 		}
 	}
    
    
    
}
