package com.example.lihm.test_tensorflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.srplab.www.starcore.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private void copyFile(Activity c, String Name,String desPath) throws IOException {
        File outfile = null;
        if( desPath != null )
            outfile = new File("/data/data/"+getPackageName()+"/files/"+desPath+Name);
        else
            outfile = new File("/data/data/"+getPackageName()+"/files/"+Name);
        //if (!outfile.exists()) {
        {
            outfile.createNewFile();
            FileOutputStream out = new FileOutputStream(outfile);
            byte[] buffer = new byte[1024];
            InputStream in;
            int readLen = 0;
            if( desPath != null )
                in = c.getAssets().open(desPath+Name);
            else
                in = c.getAssets().open(Name);
            while((readLen = in.read(buffer)) != -1){
                out.write(buffer, 0, readLen);
            }
            out.flush();
            in.close();
            out.close();
        }
    }

    StarCoreFactory starcore;
    StarServiceClass Service;
    StarSrvGroupClass SrvGroup;

    com.example.lihm.test_tensorflow.LastInputEditText textBox;
    TextView TelnetText;
    Button button1;
    Button button2;

    Handler handler;

    String CurrentPromot;
    int CurrentStartPos;

    boolean CaptureOutput = false;
    boolean DelKeyDownIsProcessed = false;

    public SharedPreferences share;
    ArrayList<String> History;
    int CurrentHistoryIndex;

    ImageButton btn_up;
    ImageButton btn_down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        share = this.getSharedPreferences("perference", Context.MODE_PRIVATE);

        History = new ArrayList<String>();
        LoadHistory();
        CurrentHistoryIndex = History.size();

        File destDir = new File("/data/data/"+getPackageName()+"/files");
        if(!destDir.exists())
            destDir.mkdirs();
        java.io.File python2_7_libFile = new java.io.File("/data/data/"+getPackageName()+"/files/python2.7.zip");
        if( !python2_7_libFile.exists() ){
            try{
                copyFile(this,"python2.7.zip",null);
            }
            catch(Exception e){
            }
        }

        try{
            copyFile(this,"zlib.so",null);
            copyFile(this,"_ctypes.so",null);
            copyFile(this,"_struct.so",null);
            copyFile(this,"itertools.so",null);
            copyFile(this,"operator.so",null);
            copyFile(this,"_collections.so",null);
            copyFile(this,"math.so",null);
            copyFile(this,"datetime.so",null);
            copyFile(this,"cPickle.so",null);
            copyFile(this,"cStringIO.so",null);
            copyFile(this,"_functools.so",null);
            copyFile(this,"time.so",null);
            copyFile(this,"_io.so",null);
            copyFile(this,"binascii.so",null);
            copyFile(this,"_random.so",null);
            copyFile(this,"future_builtins.so",null);
            copyFile(this,"_hashlib.so",null);
            copyFile(this,"_socket.so",null);
            copyFile(this,"select.so",null);
            copyFile(this,"_csv.so",null);
            copyFile(this,"parser.so",null);
            copyFile(this,"_json.so",null);
            copyFile(this,"_ssl.so",null);
            copyFile(this,"array.so",null);
            copyFile(this,"cmath.so",null);
            copyFile(this,"crypt.so",null);
            copyFile(this,"unicodedata.so",null);

            copyFile(this,"test.py",null);
        }
        catch(Exception e){
            System.out.println(e);
        }

        /*----init starcore----*/
        StarCoreFactoryPath.StarCoreCoreLibraryPath = this.getApplicationInfo().nativeLibraryDir;
        StarCoreFactoryPath.StarCoreShareLibraryPath = this.getApplicationInfo().nativeLibraryDir;
        StarCoreFactoryPath.StarCoreOperationPath = "/data/data/"+getPackageName()+"/files";

        final String LibPath = this.getApplicationInfo().nativeLibraryDir;
        final String PackagePath = "/data/data/"+getPackageName();
        final Activity GActivity = this;

        textBox = (com.example.lihm.test_tensorflow.LastInputEditText)this.findViewById(R.id.editText);
        TelnetText = (TextView)this.findViewById(R.id.textView);
        button1 = (Button)this.findViewById(R.id.button);
        button2 = (Button)this.findViewById(R.id.button2);

        CurrentPromot = "$ ";
        textBox.setText(CurrentPromot);
        TelnetText.setText("");
        CurrentStartPos = 0;

        handler = new android.os.Handler() {
            public void handleMessage(Message msg) {
                //---webview load address
                if( msg.what == 0 ) {
                    TelnetText.setText("you can connect with telnet : " + getIP() + ":3004" + "\nlogin with \"root\",pass:\"123\"");
                }
                if( msg.what == 1 ) {
                    PreExecuteScript(0);
                }
            }
        };

        new Thread(new Runnable(){
            @Override
            public void run() {
                starcore = StarCoreFactory.GetFactory();
                Service = starcore._InitSimple("test", "123", 0, 0);
                SrvGroup = (StarSrvGroupClass) Service._Get("_ServiceGroup");
                Service._CheckPassword(false);
                starcore._RegMsgCallBack_P(new StarMsgCallBackInterface(){
                    public Object Invoke(int ServiceGroupID, int uMes, Object wParam, Object lParam){
                        if (uMes == starcore._Getint("MSG_DISPMSG") || uMes == starcore._Getint("MSG_DISPLUAMSG") )
                        {
                            if( CaptureOutput == false )
                                return null;
                            final String Str = (String)wParam;
                            textBox.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    SpannableString ss = new SpannableString(Str.replace("\r",""));
                                    ss.setSpan(new ForegroundColorSpan(Color.argb(0xFF,0x69,0x69,0x69)), 0, Str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    String ss1 = textBox.getText().toString().substring(CurrentStartPos);
                                    if( ss1.equals(CurrentPromot)){
                                        //--插在中间
                                        Editable eb = textBox.getEditableText();
                                        eb.insert(textBox.length()-2,ss);
                                        eb.insert(textBox.length()-2,"\n");
                                        CurrentStartPos = textBox.getText().length() - 2;
                                    }else {
                                        textBox.append(ss);
                                        textBox.append("\n");
                                        CurrentStartPos = textBox.getText().length();
                                        textBox.append(CurrentPromot);
                                    }
                                    textBox.setSelection(textBox.length());
                                    textBox.invalidate();
                                }
                            },0);
                        }else if(uMes == 122 /*starcore._Getint("MSG_ONTELNETSTRING ") */){
                            if( !((String)lParam).equals("python")) {
                                CaptureOutput = false;
                                return null;
                            }
                            CaptureOutput = true;
                            final String Str = (String)wParam;
                            textBox.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    String[] inputs = Str.replace("\r","").split("\n");
                                    if( inputs.length == 0 )
                                        return;
                                    int Count = inputs.length;
                                    if( inputs[inputs.length-1].equals("\n") )
                                        Count = Count - 1;
                                    if( Count == 0 )
                                        return;
                                    String ss1 = textBox.getText().toString().substring(CurrentStartPos);
                                    if( !ss1.equals(CurrentPromot) || !CurrentPromot.equals("$ ")){
                                        textBox.append("\n");
                                        CurrentPromot = "$ ";
                                        textBox.append(CurrentPromot);
                                    }
                                    for( int i=0; i < Count; i++ ){
                                        if( i == 0 )
                                            textBox.append(inputs[i]);
                                        else{
                                            CurrentPromot = ". ";
                                            textBox.append("\n");
                                            textBox.append(CurrentPromot);
                                            textBox.append(inputs[i]);
                                        }
                                    }
                                    textBox.append("\n");
                                    CurrentStartPos = textBox.getText().length();
                                    CurrentPromot = "$ ";
                                    textBox.append(CurrentPromot);
                                    textBox.setSelection(textBox.length());
                                    textBox.invalidate();
                                }
                            },0);
                        }
                        return null;
                    }
                });

		        /*----run python code----*/
                SrvGroup._InitRaw("python", Service);
                StarObjectClass python = Service._ImportRawContext("python", "", false, "");
                python._Call("import", "sys");

                StarObjectClass pythonSys = python._GetObject("sys");
                StarObjectClass pythonPath = (StarObjectClass) pythonSys._Get("path");
                pythonPath._Call("insert", 0, "/data/data/" + getPackageName() + "/files/python2.7.zip");
                pythonPath._Call("insert", 0, LibPath);
                pythonPath._Call("insert", 0, "/data/data/" + getPackageName() + "/files");
                pythonPath._Call("insert", 0, "/sdcard/tensorflow/python2.7/dist-packages");
                pythonPath._Call("insert", 0, "/sdcard/tensorflow/python2.7/dist-packages/setuptools-28.7.1-py2.7.egg");

                python._Set("MainActivity", GActivity);

                SrvGroup._SetTelnetPort(3004);

                Message message1 = handler.obtainMessage();
                message1.what = 1;
                message1.obj = null;
                handler.sendMessage(message1);

                //Service._DoFile("python", "/data/data/" + getPackageName() + "/files/test.py", "");
                //--enter message loop
                int tickcount = 0;
                while (true)
                {
                    while (starcore._SRPDispatch(false) == true) ;
                    starcore._SRPUnLock();
                    try {
                        Thread.sleep(10);
                    }
                    catch(Exception ex)
                    {

                    }
                    tickcount ++;
                    if( tickcount > 100 ) {  //update ipaddress
                        tickcount = 0;
                        Message message = handler.obtainMessage();
                        message.what = 0;
                        message.obj = null;
                        handler.sendMessage(message);
                    }
                    starcore._SRPLock();
                }
            }
        }).start();

        textBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_ENTER) {
                   if( event.getAction() == KeyEvent.ACTION_UP )
                       return true;
                    if( CurrentStartPos >= textBox.getText().toString().length()){
                        CurrentPromot = "$ ";
                        textBox.append("\n");
                        CurrentStartPos = textBox.getText().length();
                        textBox.append(CurrentPromot);
                        textBox.setSelection(textBox.length());
                        textBox.invalidate();
                        return true;
                    }
                    String ss1 = textBox.getText().toString().substring(CurrentStartPos);
                    if( ss1.indexOf("$ ") < 0 ){
                        CurrentPromot = "$ ";
                        textBox.append("\n");
                        CurrentStartPos = textBox.getText().length();
                        textBox.append(CurrentPromot);
                        textBox.setSelection(textBox.length());
                        textBox.invalidate();
                        return true;
                    }
                    String s = ss1.replace(". ","").replace("$ ","");
                    //---compile toString
                    if( s.length() <= 0){
                        //---empty line
                        textBox.append("\n");
                        CurrentStartPos = textBox.getText().length();
                        textBox.append(CurrentPromot);
                        textBox.setSelection(textBox.length());
                        textBox.invalidate();
                        return true;
                    }else{
                        starcore._SRPLock();
                        Object[] result = SrvGroup._PreCompile("python", s+"\n");
                        starcore._SRPUnLock();
                        if ((boolean)result[0] == true) {
                            //执行脚本
                            CurrentPromot = "$ ";
                            textBox.append("\n");
                            CurrentStartPos = textBox.getText().length();
                            textBox.append(CurrentPromot);
                            textBox.setSelection(textBox.length());
                            textBox.invalidate();
                            CaptureOutput = true;

                            if( s.indexOf('\n') < 0 ) {
                                SetHistory(s);
                                CurrentHistoryIndex = History.size();
                            }

                            starcore._SRPLock();
                            Service._RunScript("python", s + "\n", "", "");
                            starcore._SRPUnLock();
                        }else
                        {
                            if (((String)result[1]).length() == 0) {
                                textBox.append("\n");
                                CurrentPromot = ". ";
                                textBox.append(CurrentPromot);
                                textBox.setSelection(textBox.length());
                                textBox.invalidate();
                                return true;
                            }else {
                                String Str = (String) result[1];
                                SpannableString ss = new SpannableString(Str.replace("\r",""));
                                ss.setSpan(new ForegroundColorSpan(Color.argb(0xFF,0x69,0x69,0x69)), 0, Str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                textBox.append("\n");
                                textBox.append(ss);
                                textBox.append("\n");
                                CurrentStartPos = textBox.getText().length();
                                textBox.append(CurrentPromot);
                                textBox.setSelection(textBox.length());
                                textBox.invalidate();
                            }
                        }
                    }
                    return true;
                }else if(keyCode==KeyEvent.KEYCODE_DEL) {
                    if( event.getAction() == KeyEvent.ACTION_UP ) {
                        if( DelKeyDownIsProcessed == true )
                            return true;
                        return false;
                    }
                    int index = textBox.getText().toString().lastIndexOf('\n');
                    String SubStr = textBox.getText().toString().substring(index+1);
                    if( SubStr.length() <= 2 ) {
                        if( SubStr.equals(". ")){
                            //--remove "\n. "
                            Editable editable = textBox.getText();
                            editable.delete(textBox.getText().length()-3,textBox.getText().length());
                        }
                        DelKeyDownIsProcessed = true;
                        return true;
                    }
                    DelKeyDownIsProcessed = false;
                    return false;
                }
                return false;
            }
        });

        textBox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        btn_up = (ImageButton)this.findViewById(R.id.imageButton);
        btn_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( CurrentHistoryIndex == 0 || CurrentPromot.equals(". "))
                    return;
                CurrentHistoryIndex = CurrentHistoryIndex - 1;
                int index = textBox.getText().toString().lastIndexOf('\n');
                Editable editable = textBox.getText();
                editable.delete(index+1,textBox.getText().length());
                textBox.append(CurrentPromot);
                textBox.append(History.get(CurrentHistoryIndex));
                textBox.setSelection(textBox.length());
                textBox.invalidate();
            }
        });
        btn_down = (ImageButton)this.findViewById(R.id.imageButton1);
        btn_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( CurrentHistoryIndex >= History.size() || CurrentPromot.equals(". "))
                    return;
                CurrentHistoryIndex = CurrentHistoryIndex + 1;
                int index = textBox.getText().toString().lastIndexOf('\n');
                Editable editable = textBox.getText();
                editable.delete(index+1,textBox.getText().length());
                if( CurrentHistoryIndex >= History.size() ){
                    textBox.append(CurrentPromot);
                }else{
                    textBox.append(CurrentPromot);
                    textBox.append(History.get(CurrentHistoryIndex));
                }
                textBox.setSelection(textBox.length());
                textBox.invalidate();
            }
        });

        //---
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String script = textBox.getText().toString();
                if (script.length() == 0)
                    return;
                starcore._SRPLock();
                Service._RunScript("python", script + "\n", "", "");
                starcore._SRPUnLock();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentPromot = "$ ";
                textBox.setText(CurrentPromot);
                textBox.setSelection(textBox.length());
                CurrentStartPos = 0;
            }
        });

    }

    void PreExecuteScript(final int Step)
    {
        textBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                String Buf = "";
                switch( Step)
                {
                    case 0 : Buf = "SrvGroup = libstarpy._GetSrvGroup(0)\n"; break;
                    case 1 : Buf = "Service = SrvGroup._GetService(\"\",\"\")\n"; break;
                    case 2 : Buf = "SrvGroup._InitRaw(\"java\",Service)\n"; break;
                    case 3 : Buf = "print MainActivity\n"; break;
                    case 4 : Buf = "Toast = Service._ImportRawContext(\"java\",\"android.widget.Toast\",False,\"\"); \n"; break;
                    case 5:  Buf = "Toast.makeText(MainActivity.getApplicationContext(),\"hello from python\",Toast.LENGTH_SHORT).show();\n"; break;
                    case 6 : ShowHelloMessage(); return;
                }
                textBox.append(Buf);
                CurrentStartPos = textBox.getText().length();
                textBox.append(CurrentPromot);
                textBox.setSelection(textBox.length());
                textBox.invalidate();

                CaptureOutput = true;
                starcore._SRPLock();
                Service._RunScript("python", Buf, "", "");
                starcore._SRPUnLock();

                PreExecuteScript( Step + 1 );
            }
        },200);
    }

    void ShowHelloMessage()
    {
        textBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                String Str = "demo finish....\nusing MainActivity.Run(FileName) to run python file\nyou can run python code now ...";
                SpannableString ss = new SpannableString(Str.replace("\r",""));
                ss.setSpan(new ForegroundColorSpan(Color.argb(0xFF,0x15,0x99,0x2A)), 0, Str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                String ss1 = textBox.getText().toString().substring(CurrentStartPos);
                if( ss1.equals(CurrentPromot)){
                    //--插在中间
                    Editable eb = textBox.getEditableText();
                    eb.insert(textBox.length()-2,ss);
                    eb.insert(textBox.length()-2,"\n");
                    CurrentStartPos = textBox.getText().length() - 2;
                }else {
                    textBox.append(ss);
                    textBox.append("\n");
                    CurrentStartPos = textBox.getText().length();
                    textBox.append(CurrentPromot);
                }
                textBox.setSelection(textBox.length());
                textBox.invalidate();
            }
        },200);
    }

    public void Run(String FileName)
    {
        Service._DoFile("python",FileName, "");
    }

    void SetHistory(String info)
    {
        int index = History.indexOf(info);
        if( index >= 0 )
            History.remove(index);
        History.add(info);
        if( History.size() > 64 )
            History.remove(0);
        index = 0;
        SharedPreferences.Editor editor = share.edit();//取得编辑器
        for( String item : History) {
            editor.putString("history" + index, item);
            index++;
        }
        editor.commit();//提交刷新数据
    }

    void LoadHistory()
    {
        History.clear();
        int index = 0;
        while( true ){
            if (share.getString("history"+index,"").length() == 0 )
                return;
            History.add(share.getString("history"+index,""));
            index ++;
        }
    }

    public static String getIP()
    {
        //---get ip address
        String IPAddress = null,IPAddress1 = null;
        boolean Result;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address) ) {
                        //--select 192.168
                        IPAddress1 = inetAddress.getHostAddress().toString();
                        if( IPAddress1.length() > 8 ){
                            String substr = IPAddress1.substring(0,7);
                            if( substr.equals("192.168") )
                                IPAddress = IPAddress1;
                            else{
                                if( IPAddress == null )
                                    IPAddress = IPAddress1;
                            }
                        }else{
                            if( IPAddress == null )
                                IPAddress = IPAddress1;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.println(ex.toString());
        }
        if( IPAddress == null )
            return "127.0.0.1";
        else
            return IPAddress;
    }
}
