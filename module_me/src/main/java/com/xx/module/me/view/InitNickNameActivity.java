package com.xx.module.me.view;


import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xx.lib.db.entity.UserSystem;
import com.xx.module.common.BaseConfig;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.annotation.Path;
import com.xx.module.common.annotation.Presenter;
import com.xx.module.common.model.entity.SmError;
import com.xx.module.common.router.RouterUrl;
import com.xx.module.common.view.base.BaseActivity;
import com.xx.module.me.R;
import com.xx.module.me.presenter.InitDataPresenter;
import com.xx.module.me.view.contract.IInitDataView;

import java.util.regex.Pattern;

/**
 * @author
 * @date 2017/7/10
 */
@Path(RouterUrl.ACTIVITY_ME_EDIT_NICK_NAME)
@Presenter(InitDataPresenter.class)
public class InitNickNameActivity extends BaseActivity<InitDataPresenter> implements IInitDataView {
    EditText username;
    Button commitBtn;
    TextView errorTv;

    @Override
    public int getLayoutId() {
        return R.layout.activity_init_name;
    }

    private UserSystem us;


    @Override
    protected void initViewsAndEvents() {
        commitBtn = findViewById(R.id.commit_btn);
        username = findViewById(R.id.username);
        errorTv = findViewById(R.id.username_error);
        errorTv.setVisibility(View.GONE);

        setToolBarTitle("修改昵称");
        setToolBarBack(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        us = getLoginUser();
        username.setText(us.getNickname());
        username.setSelection(username.getText().length());
        commitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = username.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    errorTv.setText("请输入昵称");
                    errorTv.setVisibility(View.VISIBLE);
                    return;
                }
                name = name.replace(" ", "");
                String first = name.substring(0, 1);
                if (TextUtils.isDigitsOnly(first)) {
                    errorTv.setText("昵称不能以数字开头");
                    errorTv.setVisibility(View.VISIBLE);
                    return;
                }

                if (name.equals(us.getNickname())) {
                    finish();
                }

                if (name.contains("用户") || name.contains("知命")) {
                    errorTv.setText("昵称不能包含[用户]或[知命]字样");
                    errorTv.setVisibility(View.VISIBLE);
                    return;
                }
                if (name.length() < 2) {
                    errorTv.setText("昵称需要2个文字以上长度");
                    errorTv.setVisibility(View.VISIBLE);
                    return;
                }
                if (!Pattern.matches(BaseConfig.ZHENGZE_NICK_NAME, name)) {
                    errorTv.setText("昵称不可包含特殊字符");
                    errorTv.setVisibility(View.VISIBLE);
                    return;
                }
                errorTv.setVisibility(View.GONE);
                us.setNickname(name);
                getPresenter().editUserNickName(us);
            }
        });
    }


    @Override
    public void onSaveUserResult(UserSystem us, SmError e) {
        if (us != null) {
            UserDataManager.getInstance().setUserSystem(us);
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            errorTv.setVisibility(View.VISIBLE);
            errorTv.setText(e.getMessage(this));
        }
    }
}
