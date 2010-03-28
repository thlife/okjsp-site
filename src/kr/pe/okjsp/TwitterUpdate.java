package kr.pe.okjsp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import kr.pe.okjsp.util.CommonUtil;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import com.rosaloves.net.shorturl.bitly.Bitly;
import com.rosaloves.net.shorturl.bitly.BitlyFactory;
import com.rosaloves.net.shorturl.bitly.url.BitlyUrl;

public class TwitterUpdate {
	
	private String tweetStsText = "" ;
	private String twitterId = "okjsp";
	private String twitterPwd = "okpass12";
	private String bitlyId = "okjsp";
	private String bitlyKey = "R_2338c002b1dfeb9d0b111d1f0bfa8ce0";
	

	public void doUpdate(Article article, HttpServletRequest req) {
		if ( !isTwitterUpdate(article, req) ){
			return;
		}
		
		String subject = article.getSubject();
		String content = article.getContent();
		int seq = article.getSeq();
		
		
		BitlyUrl bUrl = null;
        String sOrgUrl = "http://www.okjsp.pe.kr/seq/"+seq;
        URL resultUrl = null;
		try {
			resultUrl = new URL(sOrgUrl);	//원본링크
		} catch (MalformedURLException e1) {
		}
		
        try {
        	Bitly bitly = BitlyFactory.newJmpInstance(bitlyId, bitlyKey);
			bUrl = bitly.shorten(sOrgUrl);
		} catch (Exception e) {
			bUrl = null;
		}
		
        if (bUrl != null){
        	resultUrl = bUrl.getShortUrl();	//짧은링크
        }
		
		//트윗 글 올리기 임시포맷 
        //글제목: 최대30자 
		if ( subject.length() > 30 ) {
			subject = subject.substring(0, 30) + "…";
		}

		/*
		*	html tag 삭제
		*	수정 : 윤정부[Coma] 2010.03.25
		*	CommonUtil.removeTagTest 추가하여 적용함.
		*/
		content = CommonUtil.removeTag(content);
		
		if ( content.length() > 80 ) {
			content = content.substring(0, 80) + "…";			
		}
	   
		tweetStsText = subject + ": ";
		tweetStsText += content + " ";		
		tweetStsText += resultUrl;

		String result_msg = "";
		try {
			Twitter twitter = new TwitterFactory().getInstance(twitterId, twitterPwd);
	        Status status = twitter.updateStatus(tweetStsText);
	        result_msg = status.getText();
		}catch (TwitterException te) {
			te.printStackTrace();
		}

		// twitter Successfully updated 
		System.out.println("result_msg length:   "+ result_msg.length() + " & " + result_msg);
		// twitter source end
	}
	
	/**
	 * 게시판이 트위터 전송이 허용이 되었는지 확인 후 허용여부를 반환한다.
	 * 
	 * @param m_article 게시글
	 * @param m_req HttpServletRequest
	 * @return 트위터 전송 허용 여부
	 */
	private boolean isTwitterUpdate(Article m_article, HttpServletRequest m_req){
		
		try{
			HashMap bbsInfoMap = (HashMap)m_req.getSession().getServletContext().getAttribute("bbsInfoMap");
			
		    BbsInfoBean bbsInfo = ((BbsInfoBean)(bbsInfoMap.get(m_article.getBbs())));
		    if (bbsInfo == null) {
		    	System.out.println("isTwitterUpdate bbsInfo == null / m_article.getBbs() : " + m_article.getBbs());
		    	return false;
		    }
		    System.out.println("isTwitterUpdate bbsInfo.getCseq() : " + bbsInfo.getCseq());
		    if ("2".equals(bbsInfo.getCseq())) {
		    	return false;
		    }
		}catch(Exception e){
			System.out.println("isTwitterUpdate : " + e);
		}
		return true;
	}
}
