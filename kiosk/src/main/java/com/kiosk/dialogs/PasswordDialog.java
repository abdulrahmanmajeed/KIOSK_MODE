package com.kiosk.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kiosk.R;
import com.kiosk.utils.AppUtils;


public class PasswordDialog extends Dialog {

    private PasswordDialogListener passwordDialogListener;
    private Context context;
    private EditText etPassword;

    public PasswordDialog(Context context, PasswordDialogListener passwordDialogListener) {
        super(context);
        this.context = context;
        this.passwordDialogListener = passwordDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        setContentView(R.layout.dialog_password);
        setCancelable(false);

        Button btnSave = findViewById(R.id.btnSave);
        etPassword = findViewById(R.id.etPassword);
        TextView tvCancel = findViewById(R.id.tvCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.hideKeyboardFrom(context, etPassword);
                if (!TextUtils.isEmpty(etPassword.getText().toString())) {
                    passwordDialogListener.dialogAccept(etPassword.getText().toString());
                    dismiss();
                } else
                    Toast.makeText(context, context.getString(R.string.prompt_enter_value), Toast.LENGTH_SHORT).show();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.hideKeyboardFrom((Activity)context,etPassword);
                dismiss();
                passwordDialogListener.dialogDeny();
            }
        });
    }
}