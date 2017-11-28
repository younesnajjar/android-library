/* Copyright 2017 Urban Airship and Contributors */

package com.urbanairship.iam.fullscreen;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.urbanairship.R;
import com.urbanairship.iam.ButtonInfo;
import com.urbanairship.iam.InAppActionUtils;
import com.urbanairship.iam.InAppMessageActivity;
import com.urbanairship.iam.InAppMessageCache;
import com.urbanairship.iam.MediaInfo;
import com.urbanairship.iam.ResolutionInfo;
import com.urbanairship.iam.view.InAppButtonLayout;
import com.urbanairship.iam.view.InAppViewUtils;
import com.urbanairship.messagecenter.ImageLoader;

/**
 * Full screen in-app message activity.
 */
public class FullScreenActivity extends InAppMessageActivity implements InAppButtonLayout.ButtonClickListener {

    @Override
    protected void onCreateMessage(@Nullable Bundle savedInstanceState) {
        final FullScreenDisplayContent displayContent = getMessage().getDisplayContent();
        if (displayContent == null) {
            finish();
            return;
        }

        setContentView(getTemplate(displayContent.getTemplate()));
        hideActionBar();

        TextView heading = findViewById(R.id.heading);
        TextView body = findViewById(R.id.body);
        InAppButtonLayout buttonLayout = findViewById(R.id.buttons);
        ImageView imageView = findViewById(R.id.media);
        Button footer = findViewById(R.id.footer);
        final ImageButton dismiss = findViewById(R.id.dismiss);

        // Heading
        if (displayContent.getHeading() != null) {
            InAppViewUtils.applyTextInfo(heading, displayContent.getHeading());
        } else {
            heading.setVisibility(View.GONE);
        }

        // Body
        if (displayContent.getBody() != null) {
            InAppViewUtils.applyTextInfo(body, displayContent.getBody());
        } else {
            body.setVisibility(View.GONE);
        }

        // Media
        // TODO: Support video and youtube
        if (displayContent.getMedia() != null && displayContent.getMedia().getType().equals(MediaInfo.TYPE_IMAGE)) {
            String imageLocation = getCache() != null ? getCache().getBundle().getString(InAppMessageCache.MEDIA_CACHE_KEY, displayContent.getMedia().getUrl()) : displayContent.getMedia().getUrl();
            ImageLoader.shared(this).load(imageLocation, 0, imageView);
            imageView.setContentDescription(displayContent.getMedia().getDescription());
        } else {
            imageView.setVisibility(View.GONE);
        }

        // Button Layout
        if (displayContent.getButtons().isEmpty()) {
            buttonLayout.setVisibility(View.GONE);
        } else {
            buttonLayout.setButtons(displayContent.getButtonLayout(), displayContent.getButtons());
            buttonLayout.setButtonClickListener(this);
        }

        // Footer
        if (displayContent.getFooter() != null) {
            InAppViewUtils.applyButtonInfo(footer, displayContent.getFooter(), 0);
            footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onButtonClicked(view, displayContent.getFooter());
                }
            });
        } else {
            footer.setVisibility(View.GONE);
        }

        // DismissButton
        dismiss.getDrawable().setColorFilter(displayContent.getDismissButtonColor(), PorterDuff.Mode.MULTIPLY);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDisplayHandler().finished(ResolutionInfo.dismissed(getDisplayTime()));
                finish();
            }
        });
    }

    @Override
    public void onButtonClicked(View view, ButtonInfo buttonInfo) {
        InAppActionUtils.runActions(buttonInfo);
        if (buttonInfo.getBehavior().equals(ButtonInfo.BEHAVIOR_CANCEL)) {
            getDisplayHandler().cancelFutureDisplays();
        }

        getDisplayHandler().finished(ResolutionInfo.buttonPressed(buttonInfo, getDisplayTime()));
        finish();
    }

    /**
     * Gets the layout for the given template.
     *
     * @param template The fullscreen template.
     * @return The template layout resource ID.
     */
    @LayoutRes
    protected int getTemplate(@NonNull @FullScreenDisplayContent.Template String template) {
        switch (template) {
            case FullScreenDisplayContent.TEMPLATE_HEADER_BODY_MEDIA:
                return R.layout.ua_iam_fullscreen_header_body_media;

            case FullScreenDisplayContent.TEMPLATE_HEADER_MEDIA_BODY:
                return R.layout.ua_iam_fullscreen_header_media_body;

            case FullScreenDisplayContent.TEMPLATE_MEDIA_HEADER_BODY:
            default:
                return R.layout.ua_iam_fullscreen_media_header_body;
        }
    }
}
