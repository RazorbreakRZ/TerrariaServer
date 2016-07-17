/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package checkport;

/**
 *
 * @author Razorbreak
 */
public class Analyzer {
    
    public Analyzer(){
        
    }
    
    //Return the current status of the port: 1=opened 0=closed
    public int analyzeResponse(String response){
        int status = -1;
        int exist = response.lastIndexOf("Success");
        if(exist!=-1){
            status = 1;
        }else{
            status = 0;
        }
        return status;
    }
    
}
