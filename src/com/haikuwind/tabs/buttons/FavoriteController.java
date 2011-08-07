package com.haikuwind.tabs.buttons;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.haikuwind.R;
import com.haikuwind.feed.Haiku;
import com.haikuwind.feed.HaikuListData;
import com.haikuwind.feed.fetch.HttpRequest;
import com.haikuwind.notification.UpdateType;
import com.haikuwind.notification.UpdateNotifier;

public class FavoriteController extends HaikuController {
    private static final String TAG = FavoriteController.class.getSimpleName();
    private final String userId;
    
    private Context context;

    public FavoriteController(HaikuListData haikuListData, String userId, Context context) {
        super(haikuListData);
        this.userId = userId;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        Haiku haiku = getHaiku(v);
        
        if (!haiku.isFavoritedByMe()) {
            try {
                HttpRequest.favorite(haiku.getId(), userId);
                haiku.setFavoritedByMe(true);

                UpdateNotifier.fireUpdate(UpdateType.ADD_FAVORITE, haiku);
            } catch (Exception e) {
                Log.e(TAG, "error while marking favorite", e);
                Toast.makeText(context,
                        R.string.toast_error_try_again, Toast.LENGTH_SHORT).show();
            } finally {
                updateToggleFavorite(v);
            }
        }

    }

    private void updateToggleFavorite(View haikuView) {
        View toggle = haikuView.findViewById(R.id.haiku_favorite);
        if(getHaiku(haikuView).isFavoritedByMe()) {
            toggle.setBackgroundResource(R.drawable.toggle_favorite_checked);
        } else {
            toggle.setBackgroundResource(R.drawable.toggle_favorite_unchecked);
        }
    }

    public void bind(View haikuView) {
        View favoriteToggle = haikuView.findViewById(R.id.haiku_favorite);
        favoriteToggle.setVisibility(View.VISIBLE);
        
        favoriteToggle.setOnClickListener(this);
        updateToggleFavorite(haikuView);
    }
}
