package com.example.ardemo;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.content.SharedPreferences;
import android.util.Base64;

import com.example.ardemo.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 作者:created by 巴塞罗那的余晖 on 2019/3/10 18：45
 * 邮箱:zhubaoluo@outlook.com
 * 不会写BUG的程序猿不是好程序猿，嘤嘤嘤
 */
public class userData {
    private Map<String,String> root;//用户数据根目录
    private static String flag="user_data";//标识
    private  boolean loginState=false;
    private Context mcontext;
    public userData(Context context)
    {
        root=new HashMap<>();
        mcontext=context;
        readData(flag);
    }
    public boolean findExistUsername(String name)//查找是否有该用户名
    {
        return root.containsKey(name);
    }
    public boolean verifyPassword(String name,String password)//验证用户名和密码是否匹配
    {
        if(findExistUsername(name))
        {
            if(root.get(name).equals(password))
            {
                return true;
            }
        }
        return false;
    }
    public boolean getRegister(String name,String password)//注册，并返回是否注册成功
    {
        if(findExistUsername(name)||name.equals("")||password.equals(""))
        {
            return false;
        }
        root.put(name,password);
        saveData(flag);
        return true;
    }
    private void saveData(String key)//保存数据到本地
    {
        //原理：将Map格式转换成json字符串，并指定一个特定标识来记录，Value按照发生器逐一保存就好
        JSONArray mJsonArray = new JSONArray();
        Iterator<Map.Entry<String, String>> iterator = root.entrySet().iterator();
        JSONObject object = new JSONObject();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            try {
                String password_encode=entry.getValue();
                password_encode=Base64.encodeToString(password_encode.getBytes(),Base64.NO_WRAP);
                object.put(entry.getKey(), password_encode);

            } catch (JSONException e) {
                //异常处理
                //妈咪妈咪哄！BUG快离开！
            }
        }
        mJsonArray.put(object);
        SharedPreferences sp=mcontext.getSharedPreferences("config",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString(key,mJsonArray.toString());
        editor.commit();
    }
    private void readData(String key)//sharedpreferences从本地读取数据
    {
        root.clear();
        SharedPreferences sp=mcontext.getSharedPreferences("config",Context.MODE_PRIVATE);
        String result=sp.getString(key,"");
        try {
            JSONArray array=new JSONArray(result);
            for(int i=0;i<array.length();i++)
            {
                JSONObject itemObject =array.getJSONObject(i);
                JSONArray names=itemObject.names();
                if(names!=null)
                {
                    for(int j=0;j<names.length();j++)
                    {
                        String name=names.getString(j);
                        String value=itemObject.getString(name);
                        value=new String(Base64.decode(value.getBytes(),Base64.NO_WRAP));
                        root.put(name,value);
                    }
                }
            }
        }catch (JSONException e){
            //异常处理
            //妈咪妈咪哄！BUG快离开！
        }
    }
    private void clearLocalData()//清除本地数据，这个方法一般用不到，故写成私有的
    {
        SharedPreferences preferences = mcontext.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

}
