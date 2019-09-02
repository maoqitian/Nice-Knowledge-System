> 登录 注册等页面，监听EditText 都能内容Button 才能被点击等功能
## 工具类
```
public class EditTextUtils {

    static IEditTextChangeListener mChangeListener;

    public static void setChangeListener(IEditTextChangeListener changeListener) {
        mChangeListener = changeListener;
    }

    /**
     * 检测输入框是否都输入了内容
     * 从而改变按钮的是否可点击
     * 以及输入框后面的X的可见性，X点击删除输入框的内容
     */
    public static class textChangeListener{
        private Button button;
        private EditText[] editTexts;

        public textChangeListener(Button button){
            this.button=button;
        }
        public textChangeListener addAllEditText(EditText... editTexts){
            this.editTexts=editTexts;
            initEditListener();
            return this;
        }


        private void initEditListener() {
            Log.i("TAG", "调用了遍历editext的方法");
            for (EditText editText:editTexts){
                editText.addTextChangedListener(new textChange());
            }
        }



        /**
         * edit输入的变化来改变按钮的是否点击
         */
        private class textChange implements TextWatcher {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (checkAllEdit()){
                    Log.i("TAG", "所有edittext有值了");
                    mChangeListener.textChange(true);
                    button.setEnabled(true);
                }else {
                    button.setEnabled(false);
                    Log.i("TAG", "有edittext没值了");
                    mChangeListener.textChange(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        }

        /**
         * 检查所有的edit是否输入了数据
         * @return
         */
        private boolean checkAllEdit() {
            for (EditText editText:editTexts){
                if (!TextUtils.isEmpty(editText.getText() + "")){
                    continue;
                }else {
                    return false;
                }
            }
            return true;
        }
    }

    public interface IEditTextChangeListener{
       void textChange(boolean isAllHaveText);
    }
}

```
## 使用
```
EditTextUtils.textChangeListener textChangeListener = new EditTextUtils.textChangeListener(mLogin);
        textChangeListener.addAllEditText(mUserName,mPassword);
        EditTextUtils.setChangeListener(isAllHaveText -> {
            if(isAllHaveText){ //都已经有内容 登陆按钮可以点击
             //执行操作
            }else { //有一个editText无内容 登陆按钮不可以点击
            //执行操作
            }
        });
```