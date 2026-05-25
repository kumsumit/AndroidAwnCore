package me.carda.awesome_notifications.core.media;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.SimpleBasePlayer;
import androidx.media3.common.util.UnstableApi;

import com.google.common.collect.ImmutableList;

import me.carda.awesome_notifications.core.enumerators.NotificationPlayState;
import me.carda.awesome_notifications.core.models.NotificationContentModel;

@UnstableApi
public final class NotificationMediaSessionPlayer extends SimpleBasePlayer {
    private State state;

    public NotificationMediaSessionPlayer(@NonNull Context context) {
        super(context.getApplicationContext().getMainLooper());
        state = createState(null);
    }

    public void update(@Nullable NotificationContentModel contentModel) {
        state = createState(contentModel);
        invalidateState();
    }

    @Override
    protected State getState() {
        return state;
    }

    private State createState(@Nullable NotificationContentModel contentModel) {
        if (contentModel == null) {
            return new State.Builder()
                    .setPlaybackState(Player.STATE_IDLE)
                    .build();
        }

        MediaMetadata metadata = createMetadata(contentModel);
        MediaItem mediaItem = new MediaItem.Builder()
                .setMediaId(String.valueOf(contentModel.id == null ? 0 : contentModel.id))
                .setMediaMetadata(metadata)
                .build();

        long durationMs = contentModel.duration == null || contentModel.duration < 0
                ? C.TIME_UNSET
                : contentModel.duration * 1000L;
        long positionMs = getPositionMs(contentModel, durationMs);

        SimpleBasePlayer.MediaItemData mediaItemData =
                new SimpleBasePlayer.MediaItemData.Builder(/* uid= */ mediaItem.mediaId)
                        .setMediaItem(mediaItem)
                        .setMediaMetadata(metadata)
                        .setDurationUs(durationMs == C.TIME_UNSET ? C.TIME_UNSET : durationMs * 1000L)
                        .build();

        return new State.Builder()
                .setAvailableCommands(Player.Commands.EMPTY)
                .setPlayWhenReady(isPlaying(contentModel.playState), Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
                .setPlaybackState(toMedia3PlaybackState(contentModel.playState))
                .setPlaybackParameters(new PlaybackParameters(getPlaybackSpeed(contentModel)))
                .setPlaylist(ImmutableList.of(mediaItemData))
                .setCurrentMediaItemIndex(0)
                .setContentPositionMs(positionMs)
                .setContentBufferedPositionMs(SimpleBasePlayer.PositionSupplier.getConstant(positionMs))
                .build();
    }

    private MediaMetadata createMetadata(@NonNull NotificationContentModel contentModel) {
        MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
        if (contentModel.title != null) {
            metadataBuilder.setTitle(contentModel.title);
            metadataBuilder.setDisplayTitle(contentModel.title);
        }
        if (contentModel.body != null) {
            metadataBuilder.setArtist(contentModel.body);
            metadataBuilder.setSubtitle(contentModel.body);
        }
        if (contentModel.duration != null && contentModel.duration >= 0) {
            metadataBuilder.setDurationMs(contentModel.duration * 1000L);
        }
        return metadataBuilder.build();
    }

    private long getPositionMs(@NonNull NotificationContentModel contentModel, long durationMs) {
        if (contentModel.progress == null || durationMs == C.TIME_UNSET) {
            return 0L;
        }
        float progress = Math.max(0f, Math.min(100f, contentModel.progress));
        return (long) (durationMs * (progress / 100f));
    }

    private float getPlaybackSpeed(@NonNull NotificationContentModel contentModel) {
        if (contentModel.playbackSpeed == null || contentModel.playbackSpeed < 0f) {
            return 0f;
        }
        return contentModel.playbackSpeed;
    }

    private boolean isPlaying(@Nullable NotificationPlayState playState) {
        return playState == NotificationPlayState.playing ||
                playState == NotificationPlayState.forwarding ||
                playState == NotificationPlayState.rewinding ||
                playState == NotificationPlayState.skippingToQueueItem ||
                playState == NotificationPlayState.next ||
                playState == NotificationPlayState.previous;
    }

    private int toMedia3PlaybackState(@Nullable NotificationPlayState playState) {
        if (playState == null) {
            return Player.STATE_READY;
        }

        switch (playState) {
            case buffering:
            case connecting:
                return Player.STATE_BUFFERING;
            case stopped:
            case none:
                return Player.STATE_ENDED;
            case error:
                return Player.STATE_IDLE;
            case unknown:
            default:
                return Player.STATE_READY;
        }
    }
}
