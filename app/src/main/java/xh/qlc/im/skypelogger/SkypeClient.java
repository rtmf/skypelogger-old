package xh.qlc.im.skypelogger;

import android.text.format.Time;
import android.text.style.URLSpan;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.TimeZone;

/**
 * Created by rtmf on 8/8/15.
 */
public class SkypeClient {
    final String CMAG = "578134";
    final String LKID = "msmsgs@msnmsgr.com";
    final String LKEY = "Q1P7W2E4J9R8U3S5";
    final Integer MNUM = 0x0E79A9C1;
    final String NAME = "swx-skype.com";
    final String VERS = "908/1.7.251";
    final String HMDV = "client-s.gateway.messenger.live.com";
    final String HLOG = "login.skype.com";
    final String HCON = "api.skype.com";
    final String HCNV = "contacts.skype.com";
    final String HVID = "vm.skype.com";

    AsyncHttpClient ac;
    PersistentCookieStore cs;
    String hmsg;
    String nick;
    String unam;
    String fnam;
    String lnam;
    Integer tarq;
    LoginActivity la;
    String stok;
    String rtok;
    String epid;
    Integer rexp;
    String spie;
    String setm;
    String ssid;
    String spwd;

    public SkypeClient(LoginActivity loginActivity) {
        la = loginActivity;
        ac = new AsyncHttpClient();
        ac.setEnableRedirects(true, false);
    }
    public void hADD(String h, String v) {
        ac.addHeader(h, v);
    }
    public void hSET(String h, String v) {
        lti("head",h+": "+v);
        ac.removeHeader(h);
        hADD(h, v);
    }
    public void hRST() {
        ac.removeAllHeaders();
        hSET("Accept-Encodings", "gzip");
        hSET("Connection", "close");
    }
    public void hCLI() {
        hSET("ClientInfo", "os=Windows; osVer=8.1; proc=Win32; lcid=en-us; deviceType=1; country=n/a; clientName=" + NAME + "; clientVer=" + VERS);
    }
    public void hXFU() {
        hSET("Content-Type", "Content-Type: application/x-www-form-urlencoded; charset=UTF-8");
    }
    public void hALL() {
        hSET("Accept", "*/*");
    }
    public void h404() {
        hSET("BehaviourOverride", "redirectAs404");
    }
    public void hLOG() {
        hRST();
        hCLI();
        hXFU();
        hALL();
        h404();
    }
    public Long ites() {
        return System.currentTimeMillis()/1000;
    }
    public String stes() {
        return ites().toString();
    }
    public String mshash(String base) {
        try {
            int i;
            StringBuffer data=new StringBuffer();
            data.append(base);
            data.append(LKEY);
            MessageDigest digest;
            byte[] sha256;
                digest = MessageDigest.getInstance("SHA-256");
                sha256 = digest.digest(data.toString().getBytes("UTF-8"));
            byte[] hash = Arrays.copyOfRange(sha256, 0, 16);
            StringBuffer hexString = new StringBuffer();
            for (i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            int[] shai = new int[4];
            for (i = 0; i < 4 ; i++)
            {
                String iString=hexString.substring(i*8,(i+1)*8);
                shai[i]=(int)(Long.parseLong(iString,16) & 0x7fffffff);
            }
            data = new StringBuffer();
            hexString = new StringBuffer();
            data.append(base);
            data.append(LKID);
            for (i = 0; i < (8-(data.toString().getBytes("UTF-8").length%8)); i++) {
                data.append("0");
            }
            byte[] dbyt=data.toString().getBytes("UTF-8");
            int[] chli = new int[dbyt.length/8];
            for (i = 0; i < dbyt.length; i++) {
                String hex = Integer.toHexString(0xff & dbyt[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            for (i = 0; i < chli.length ; i++) {
                String iString=hexString.substring(i * 8, (i + 1) * 8);
                chli[i]=(int)(Long.parseLong(iString,16) & 0x7fffffff)
                ;
            }
            long high=0;
            long low=0;
            long temp=0;
            int mnum=MNUM.intValue();
            for (i = 0; i < chli.length-1 ; i+=2) {
                temp=chli[i];
                temp=(mnum*temp) % 0x7fffffff;
                temp+=high;
                temp=shai[0]*temp+shai[1];
                temp%=0x7fffffff;
                high=chli[i+1];
                high=(high*temp) % 0x7fffffff;
                high=shai[2]*high+shai[3];
                high%=0x7fffffff;
                low+=high+temp;
            }
            high=(high+shai[1])%0x7fffffff;
            low=(low+shai[3])%0x7fffffff;
            //maybe little-endian?
            shai[0]^=low;
            shai[1]^=high;
            shai[2]^=low;
            shai[3]^=high;
            hexString=new StringBuffer();
            for (i=0; i<4; i++) {
                String hex=Integer.toHexString(shai[i]);
                for (int j=0; j<(4-hex.length());j++)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    public void hLAK() {
        String t=stes();
        hSET("LockAndKey", "appId=" + LKID + "; time=" + t + "; lockAndKeyResponse=" + mshash(t));
    }
    public void hAST() {
        hSET("Authentication", "skypetoken=" + stok);
    }
    public void hJSN() {
        hSET("Content-Type", "application/json; ver=1.0;");
    }
    public void hAJS() {
        hSET("Accept", "application/json; ver=1.0;");
    }
    public void hHST(String h) { hSET("Host", h); }
    public void hTOK() {
        hRST();
        hCLI();
        hLAK();
        hAST();
        hJSN();
        hALL();
        h404();
        hHST(hmsg);
    }
    public void hXST() {
        hSET("X-Skypetoken", stok);
    }
    public void hXSC() {
        hSET("X-Stratus-Caller", NAME);
    }
    public void hXSR() {
        hSET("X-Stratus-Request", "abcd1234");
    }
    public void hORI() {
        hSET("Origin", "https://web.skype.com/");
    }
    public void hREF() {
        hSET("Referer", "https://web.skype.com/main");
    }
    public void hCON() {
        hRST();
        hXST();
        hXSC();
        hXSR();
        hORI();
        hREF();
        hJSN();
    }
    public void hTKN() {
        hSET("RegistrationToken", rtok);
    }
    public void hMSG() {
        hRST();
        hREF();
        hTKN();
        hCLI();
        hAJS();
    }
    public void hDEF() {
        hRST();
        hALL();
    }
    public void reset() {
        stok = "";
        tarq = 0;
        hmsg = HMDV;
        rtok = "";
        epid = "";
        rexp = 0;
        spie = "";
        setm = "";
    }
    public void lti(String tag, String text) {
        Log.i(tag, text);
        Toast.makeText(la, "[" + tag + "]:" + text, Toast.LENGTH_SHORT).show();
    }
    public void lte(String tag, String text, Throwable error) {
        Log.e(tag, text, error);
        Toast.makeText(la, "[" + tag + "]<!" + error.toString() + ">:" + text, Toast.LENGTH_SHORT).show();
    }
    public void lte(String tag, String text, String error) {
        Log.e(tag, "<!"+error+">:"+text);
        Toast.makeText(la, "[" + tag + "]<!" + error + ">:" + text, Toast.LENGTH_SHORT).show();
    }
    public String rbgv(String body,String name) {
        String rivm="\""+name+"\" value=\"";
        int rbvs=body.indexOf(rivm);
        if (rbvs==-1) {
            lte(name,body,"NotFound");
            return null;
        }
        rbvs+=rivm.length();
        return body.substring(rbvs,body.indexOf('"',rbvs));
    }
    public void subs() {
        try {
            JSONObject je = new JSONObject();
            je.put("template","raw");
            je.put("channelType", "httpLongPoll");
            JSONArray ja = new JSONArray();
            ja.put("/v1/users/ME/conversations/ALL/properties");
            ja.put("/v1/users/ME/conversations/ALL/messages");
            ja.put("/v1/users/ME/contacts/ALL");
            ja.put("/v1/threads/ALL");
            je.put("interestedResources", ja);
            lti("subs", je.toString());
            hMSG();
            String uri="https://"+hmsg+"/v1/users/ME/endpoints/"+epid+"/subscriptions";
            lti("subs",uri);
            String jes=je.toString();
            ByteArrayEntity be=new ByteArrayEntity(jes.getBytes());
            ac.post(null, uri, be, "application/json; ver=1.0;", new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            sync();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            lte("subs", Integer.toString(statusCode), error);
                        }
                    });
        } catch (Exception e) {
            lte("subs","OOPS! JSON FAIL!",e);
        }
    }
    public void rtcb(Header[] headers, byte[] r) {
        lti("rtcb", "RTCB!");
        for (Header h: headers) {
            if (h.getName().toString().contentEquals("Set-RegistrationToken")) {
                String[] rv = h.getValue().split("; ");
                rtok = rv[0];
                rexp = Integer.parseInt(rv[1].split("=")[1]);
                epid = rv[2].split("=")[1].replace("{","%7B").replace("}","%7D");
                lti("rtcb",rtok);
            } else if (h.getName().toString().contentEquals("Location")) {
                try {
                    URI l = new URI(h.getValue());
                    if (!hmsg.contentEquals(l.getHost())) {
                        hmsg = l.getHost();
                        token();
                        return;
                    }
                } catch (Exception e) {
                    lte("rtcb","Bad Location Header",e);
                }
            }
        }
        subs();
    }
    public void token() {
        rtok="";
        epid="";
        hTOK();
        HttpEntity he = new ByteArrayEntity("{}".getBytes());
        ac.post(la, "https://" + hmsg + "/v1/users/ME/endpoints", he, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                rtcb(headers, responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                lte("token", Integer.toString(statusCode), error);
            }
        });
    }
    public void getid() {
        unam="";
        fnam="";
        lnam="";
        nick="";
        hCON();
        ac.get(la, "https://" + HCON + "/users/self/displayname", new ByteArrayEntity("{}".getBytes()), "application/json",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        unam=response.optString("username","");
                        fnam=response.optString("firstname","");
                        lnam=response.optString("lastname","");
                        nick=response.optString("displayname","");
                        if(nick.contentEquals(""))
                            nick=fnam+" "+lnam;
                        lti("getid",nick);
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                        lte("getid",Integer.toString(statusCode),error);
                    }
                });
    }

    public void sync() {
        if (rtok.contentEquals("")) {
            token();
        } else {
            getid();
        }
    }
    public void doauth() {
        cs = new PersistentCookieStore(la);
        ac.setCookieStore(cs);
        hLOG();
        RequestParams rp=new RequestParams();
        rp.put("client_id",CMAG);
        rp.put("redirect_uri","https://web.skype.com");
        String qs=rp.toString();
        rp.put("username",ssid);
        rp.put("password", spwd);
        rp.put("pie",spie);
        rp.put("etm",setm);
        rp.put("js_time",System.currentTimeMillis());
        int z= TimeZone.getDefault().getRawOffset();
        String zf="+|";
        if (z<0) {
            z=-z;
            zf="-|";
        }
        z/=60000;
        zf+=Integer.toString(z/60)+"|"+Integer.toString(z%60);
        rp.put("timezone_field",zf);
        ac.post("https://" + HLOG + "/login?" + qs, rp, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String s) {
                stok=rbgv(s,"skypetoken");
                if(stok!=null)
                    sync();
                else
                    lte("doauth","Here's where I should be showing you a reCaptcha","CAPTCHANotImpl");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                lte("doauth",Integer.toString(statusCode),error);
            }
        });
    }
    public void login(String lsid, String lpwd) {
        reset();
        hRST();
        ssid=lsid;
        spwd=lpwd;
        ac.get("https://" + HLOG + "/login?method=skype&client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com", new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String s) {
                spie=rbgv(s,"pie");
                setm=rbgv(s,"etm");
                if (spie != null) {
                    doauth();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                lte("login",Integer.toString(statusCode),error);
            }
        });
    }
}
