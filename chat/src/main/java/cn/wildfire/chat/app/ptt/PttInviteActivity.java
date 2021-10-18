/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.app.ptt;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import cn.wildfire.chat.kit.conversation.forward.ForwardPromptView;
import cn.wildfire.chat.kit.conversation.pick.PickOrCreateConversationActivity;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.viewmodel.MessageViewModel;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.PttInviteMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.ptt.ChannelInfo;
import cn.wildfirechat.ptt.PTTClient;

public class PttInviteActivity extends PickOrCreateConversationActivity {
    private String channelId;
    private MessageViewModel messageViewModel;
    private UserViewModel userViewModel;
    private GroupViewModel groupViewModel;

    @Override
    protected void afterViews() {
        super.afterViews();
        channelId = getIntent().getStringExtra("channelId");
        messageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
    }

    @Override
    protected void onPickOrCreateConversation(Conversation conversation) {
        invite(conversation);
    }

    public void invite(Conversation conversation) {
        switch (conversation.type) {
            case Single:
                UserInfo userInfo = userViewModel.getUserInfo(conversation.target, false);
                invite(userInfo.displayName, userInfo.portrait, conversation);
                break;
            case Group:
                GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
                invite(groupInfo.name, groupInfo.portrait, conversation);
                break;
            default:
                break;
        }

    }

    private void invite(String targetName, String targetPortrait, Conversation targetConversation) {
        ForwardPromptView view = new ForwardPromptView(this);
        view.bind(targetName, targetPortrait, "对讲邀请");
        MaterialDialog dialog = new MaterialDialog.Builder(this)
            .customView(view, false)
            .negativeText("取消")
            .positiveText("发送")
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Message extraMsg = null;
                    if (!TextUtils.isEmpty(view.getEditText())) {
                        TextMessageContent content = new TextMessageContent(view.getEditText());
                        extraMsg = new Message();
                        extraMsg.content = content;
                    }
                    ChannelInfo channelInfo = PTTClient.getInstance().getChannelInfo(channelId);
                    PttInviteMessageContent inviteMessage = new PttInviteMessageContent(channelId, null, channelInfo.name, null, null);
                    messageViewModel.sendMessage(targetConversation, inviteMessage);
                    Toast.makeText(PttInviteActivity.this, "邀请成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .build();
        dialog.show();
    }
}
