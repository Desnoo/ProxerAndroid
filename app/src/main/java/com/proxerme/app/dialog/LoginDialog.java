package com.proxerme.app.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.proxerme.app.R;
import com.proxerme.app.util.ErrorHandler;
import com.proxerme.library.connection.ProxerConnection;
import com.proxerme.library.connection.ProxerException;
import com.proxerme.library.entity.LoginUser;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Todo: Describe Class
 *
 * @author Ruben Gees
 */
public class LoginDialog extends MainDialog<LoginDialog.LoginDialogCallback> {

    ViewGroup root;

    @Bind(R.id.dialog_login_username_container)
    TextInputLayout usernameInputContainer;
    @Bind(R.id.dialog_login_password_container)
    TextInputLayout passwordInputContainer;

    @Bind(R.id.dialog_login_username)
    EditText usernameInput;
    @Bind(R.id.dialog_login_password)
    EditText passwordInput;

    @Bind(R.id.dialog_login_input_container)
    ViewGroup inputContainer;

    @Bind(R.id.dialog_login_progress)
    ProgressBar progress;

    private boolean loading;

    public static LoginDialog newInstance() {
        return new LoginDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        findViews();
        initViews();

        if (savedInstanceState != null) {
            handleVisibility();
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext()).autoDismiss(false)
                .title(R.string.dialog_login_title).positiveText(R.string.dialog_login_go)
                .negativeText(R.string.dialog_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        login();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                }).customView(root, true);

        return builder.build();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        ProxerConnection.cancelSync(ProxerConnection.TAG_LOGIN, false);
    }

    private void findViews() {
        root = (ViewGroup) View.inflate(getContext(), R.layout.dialog_login, null);
        ButterKnife.bind(this, root);
    }

    private void initViews() {
        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    login();
                    return true;
                }
                return false;
            }
        });

        usernameInput.addTextChangedListener(new OnTextListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetError(usernameInputContainer);
            }
        });

        passwordInput.addTextChangedListener(new OnTextListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetError(passwordInputContainer);
            }
        });
    }

    private void login() {
        if (!loading) {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (checkInput(username, password)) {
                loading = true;
                handleVisibility();

                ProxerConnection.login(new LoginUser(username, password),
                        new ProxerConnection.ResultCallback<LoginUser>() {
                            @Override
                            public void onResult(@NonNull LoginUser user) {
                                if (getCallback() != null) {
                                    getCallback().onLogin(user);
                                }

                                dismiss();
                            }

                            @Override
                            public void onError(@NonNull ProxerException e) {
                                Toast.makeText(getContext(),
                                        ErrorHandler.getMessageForErrorCode(getContext(),
                                                e.getErrorCode()), Toast.LENGTH_LONG).show();

                                loading = false;
                                handleVisibility();
                            }
                        });
            }
        }
    }

    private void handleVisibility() {
        if (loading) {
            inputContainer.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        } else {
            inputContainer.setVisibility(View.VISIBLE);
            progress.setVisibility(View.INVISIBLE);
        }
    }

    private boolean checkInput(@NonNull String username, @NonNull String password) {
        boolean inputCorrect = true;


        if (TextUtils.isEmpty(username)) {
            inputCorrect = false;

            setError(usernameInputContainer,
                    getContext().getString(R.string.dialog_login_error_no_username));
        }

        if (TextUtils.isEmpty(password)) {
            inputCorrect = false;

            setError(passwordInputContainer,
                    getContext().getString(R.string.dialog_login_error_no_password));
        }
        return inputCorrect;
    }

    private void setError(@NonNull TextInputLayout container, @NonNull String error) {
        container.setError(error);
        container.setErrorEnabled(true);
    }

    private void resetError(@NonNull TextInputLayout container) {
        container.setError(null);
        container.setErrorEnabled(false);
    }

    public interface LoginDialogCallback {
        void onLogin(LoginUser user);
    }

    private static abstract class OnTextListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}