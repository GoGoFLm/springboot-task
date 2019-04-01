package com.iask.seo.task;


import java.text.DecimalFormat;
import java.util.Date;
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
 * <p>SEO系统-跑价值得分</p>
 * 【定义多少个 @Scheduled ， 等于定义多少个线程】
 * 
 * @author flm
 *
 */
@Component
public class SeoValueTask {
	
	private final static Logger logger = LoggerFactory.getLogger(SeoValueTask.class);
	private static final long SECOND = 1000 ;
	private static final long SECOND_EIGHT = 1000*8 ;
	
	@Value("${spring.http.url.pull}")
	private String URL_PULL;
	@Value("${spring.http.url.push}")
	private String URL_PUSH;
	@Value("${task.sleep.msecond}")
	private int TASK_SLEEP;
	
	
    @Scheduled(initialDelay = SECOND_EIGHT * 3, fixedDelay = SECOND*2 )  
    public void runRankJob1() throws InterruptedException {
    	runValue("1");
    }
    
    @Scheduled(initialDelay = SECOND_EIGHT * 4, fixedDelay = SECOND*2)       
    public void runRankJob2() throws InterruptedException {
    	runValue("2");
    }
    
    @Scheduled(initialDelay = SECOND_EIGHT * 5, fixedDelay = SECOND*2)       
    public void runRankJob3() throws InterruptedException {
    	runValue("3");
    }
    
    @Scheduled(initialDelay = SECOND_EIGHT * 7, fixedDelay = SECOND*2 )       
    public void runRankJob4() throws InterruptedException {
    	runValue("4");
    }
    
    @Scheduled(initialDelay = SECOND_EIGHT * 8, fixedDelay = SECOND*2 )       
    public void runRankJob5() throws InterruptedException {
    	runValue("5");
    }
    
    
    
    
    
    // 价值得分 --多线程thread调用不能一样
 	private void runValue(String thread) throws InterruptedException {
 		logger.info("runValue  thread :" + thread);
 		DecimalFormat format  = new DecimalFormat("##0.00");   
 		
 		try {
 			String key = RedisKey.SEO_WORD_VALUE;
 			Map<String, Object> mapParam = new HashMap<String, Object>();
 			mapParam.put("key",key);
 			mapParam.put("thread",thread);
 			
 			String sr = BaseHelper.post(URL_PULL, mapParam);
 			Gson gson = new Gson();
 			NoticeBean noticeBean = gson.fromJson(sr, new TypeToken<NoticeBean>(){}.getType());
 			
 			if( !"0".equals(noticeBean.getCode()) || noticeBean.getObject() == null || noticeBean.getObject().size()<1){
 				logger.info("runValue no data ... :"+noticeBean.getCode()+ "    ,  sleep ms:" +TASK_SLEEP);
 				Thread.sleep( TASK_SLEEP );					// 没有数据时 ，睡眠20分钟，防止过分请求后台压力大
 				return;
 			}
 			
 			List<SEOWordLibrary> libraries =gson.fromJson(gson.toJson(noticeBean.getObject()), new TypeToken<List<SEOWordLibrary>>(){}.getType());
 			
 			int i = 0;
 			for(SEOWordLibrary library : libraries){
 				i++;
 				logger.info("SeoKeyWordJob runValue word  关键词Id:"+library.getId() + "  ....  总数："+libraries.size()+"   跑到第#i:"+i);
 				
 					try {
					
					int all = BaseHelper.getSearchAll(library.getName());					// 收录量得分
					float ory = BaseHelper.getWenwoOriginality(library.getName());			//竞争度得分
					float search =  BaseHelper.getSearchValue(library.getPcSearch(), library.getmSearch());  // 检索量得分
					
					float value = all*1f + search*5f +((ory*8f)/3f);					  	// 根据各种值求得 ，价值得分 
					library.setScore(Float.valueOf(format.format(value)));
					
					logger.info("runValue ... 得分score ："+value);
					
				} catch (Exception e) {
					logger.info("runValue score  error....",e);
				}
 			}
 			
 			// 返回数据给 后台
 			Map<String, Object> mapParam2 = new HashMap<String, Object>();
 			mapParam2.put("key",key);
 			mapParam2.put("thread",thread);
 			mapParam2.put("msg", gson.toJson(libraries));
 			String rt = BaseHelper.post(URL_PUSH, mapParam2);
 			
 			logger.info("runValue push rt:"+rt);
 			
 		} catch (Exception e) {
 			logger.error("runValue  run error ..."+ "  sleep ms:" +TASK_SLEEP,e);
 			Thread.sleep( TASK_SLEEP );
 		}
 	}
    
    
    
}
