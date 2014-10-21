package com.tmoncorp.component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/spring/spring-application-context.xml" })
public class EventGenerator {

//	private static Logger log = LoggerFactory.getLogger(EventGenerator.class);

	@Test
	public void doRun()  throws JsonGenerationException, JsonMappingException, IOException, InterruptedException {
		
		ObjectMapper mapper = new ObjectMapper();

		Map<String, Object> jsondata = new HashMap<String, Object>();
		jsondata.put("type", "send");
		jsondata.put("campaign", 16010);
		jsondata.put("mNo", 25247721);
		jsondata.put("token", "740d4efc60e7217269c16a20c0ba540403a1f876f015ec633360e035523675c2");
		jsondata.put("isSuccess", 1);
		String jsonString = URLEncoder.encode(mapper.writeValueAsString(jsondata), "utf-8");

		URL url = new URL("http://localhost:8888/"+ jsonString);

		for(int i=0; i<10000; i++) {
			URLConnection conn = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));

			String inputline;
			while((inputline = reader.readLine()) != null)
			System.out.println(inputline);

			reader.close();
		}

	}
	
}
