package com.wordcount.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.wordcount.util.TextSearchService;

@RestController
public class WordCountController {
	
	@Autowired
	TextSearchService searchService;
	
	/**
	 * Service to search count of given JSON elements 
	 * @param searchText
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/search", method = RequestMethod.POST,headers="Accept=application/json")
	public Map<String, Map<String,Integer>> searchText(@RequestBody String searchText) throws JsonParseException, JsonMappingException, IOException {
		
		System.out.println("Inside searchText method ");
		System.out.println("searchText param value : "+searchText);
		
		Map<String, Map<String,Integer>> resultMap = new HashMap<String, Map<String,Integer>>();
		Map<String,Integer> searchTextMap = null;
		
		if(null!=searchText && ""!=searchText){
			
			searchTextMap = searchService.searchText(searchText);
			
		}else{
			resultMap.put("Empty Search String", searchTextMap);
		}
		
		resultMap.put("counts", searchTextMap);
		

		return resultMap;

	}
	
	
	/**
	 * Service to generate top count of tokens as per input
	 * @param count
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/top/{count}", method = RequestMethod.GET,headers="Accept=application/csv")
	public void getUserHistory(@PathVariable int count,HttpServletResponse response) throws IOException {
		
		
		System.out.println("Top "+count+" tokens to be found");
		
			File newFile = searchService.returnCount(count);

			 
		        InputStream is = new FileInputStream(newFile);
		 
		        // MIME type of the file
		        response.setContentType("application/octet-stream");
		        // Response header
		        response.setHeader("Content-Disposition", "attachment; filename=\""
		                + newFile.getName() + "\"");
		        // Read from the file and write into the response
		        OutputStream os = response.getOutputStream();
		        byte[] buffer = new byte[1024];
		        int len;
		        while ((len = is.read(buffer)) != -1) {
		            os.write(buffer, 0, len);
		        }
		        os.flush();
		        os.close();
		        is.close();
		
		

	}
	
	
}
