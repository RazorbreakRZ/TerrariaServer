/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package checkport;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Razorbreak
 */
public class URLConection extends Thread{
    
    private String response;
    
    public URLConection(){
    }
    
    public int doSubmit(String url,Map<String,String> data){
        try{
            URL siteUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) siteUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());

            Set keys = data.keySet();
            Iterator keyIter = keys.iterator();
            String content = "";
            for(int i=0; keyIter.hasNext(); i++) {
                    Object key = keyIter.next();
                    if(i!=0) {
                            content += "&";
                    }
                    content += key + "=" + URLEncoder.encode(data.get(key),"UTF-8");
            }
            //System.out.println(content);
            out.writeBytes(content);
            out.flush();
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            while((line=in.readLine())!=null) {
                response += line+"\n";
                //System.out.println(line);
            }
            in.close();
            return 0;
        }catch(Exception e){
            //System.err.println(e);
            return 1;
        }
    }
    
    public String getResponse(){
        return this.response;
    }
}
