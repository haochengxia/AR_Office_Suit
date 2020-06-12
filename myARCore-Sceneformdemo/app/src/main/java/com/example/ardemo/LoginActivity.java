package com.example.ardemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ardemo.userData;


public class LoginActivity extends AppCompatActivity {
    //声明变量
    private Boolean login_state=false;
    private TextView mregister;
    private ImageView mimageView;
    private ImageButton mimageButton;
    private EditText muesername, mpassword;
    private Button mconfirm;
    private userData data;
    private CheckBox checkBox;
    static public String userName = "default";
    private String LOCAL_USER_NAME="paul";
    private String portraitURL = "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2281052020,3958255485&fm=27&gp=0.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeStatusBarTransparent(this);
        setContentView(R.layout.activity_login);
        //找布局控件对应ID
        mimageView = findViewById(R.id.iv_1);
        mimageButton = findViewById(R.id.im_qq);
        muesername = findViewById(R.id.et_username);
        mpassword = findViewById(R.id.et_password);
        mconfirm = findViewById(R.id.btn_confirm);
        mregister = findViewById(R.id.btn_register);
        checkBox=findViewById(R.id.cb_rm);
        data=new userData(LoginActivity.this);
        //Glide.with(LoginActivity.this).load(portraitURL).into(mimageView);
        initLogin();
        mregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    if(data.getRegister(muesername.getText().toString(),mpassword.getText().toString()))
                    {
                        Toast.makeText(LoginActivity.this,"注册成功！",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,"注册失败！用户名重复或者为空！",Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
        mconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(data.verifyPassword(muesername.getText().toString(),mpassword.getText().toString()))
                {
                    Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                    saveLogin(login_state);
                    startActivity(intent);
                    //Toast.makeText(LoginActivity.this,muesername.getText().toString()+"欢迎您！",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(LoginActivity.this,"登陆失败！请检查用户是否存在或者密码错误！",Toast.LENGTH_SHORT).show();
                }
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                login_state=isChecked;
            }
        });
    }
    private void saveLogin(boolean flag)
    {
        SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences("ACCOUNT_REMEMBER", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String secreat_name = muesername.getText().toString();
        String secreat_password=mpassword.getText().toString();
        userName = secreat_name;
        if(sharedPreferences.getBoolean("flag",false))
        {
            return;
            //验证通过，不需要再次保存了
        }
        if(flag) {
            secreat_password=Base64.encodeToString(secreat_password.getBytes(),Base64.NO_WRAP);
            editor.putString("name",secreat_name);
            editor.putString("password", secreat_password);
            editor.putBoolean("flag", flag);
            editor.commit();
        }
        else{
            editor.clear();
            editor.putString("name",secreat_name);
            editor.putBoolean("flag",false);
            editor.commit();
        }

    }
    private void cleanState()
    {
        SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences("ACCOUNT_REMEMBER", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
    private void initLogin()
    {
        SharedPreferences sharedPreferences=LoginActivity.this.getSharedPreferences("ACCOUNT_REMEMBER", MODE_PRIVATE);
        if(sharedPreferences.getBoolean("flag",false)){
            String decode_password=sharedPreferences.getString("password","");
            decode_password= new String(Base64.decode(decode_password.getBytes(),Base64.NO_WRAP));
            muesername.setText(sharedPreferences.getString("name",""));
            mpassword.setText(decode_password);
            checkBox.setChecked(true);
        }
        else {
            muesername.setText(sharedPreferences.getString("name",""));
            checkBox.setChecked(false);
        }
    }

    public static void makeStatusBarTransparent(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            int option = window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            window.getDecorView().setSystemUiVisibility(option);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
