package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class PlaylistInfo extends ListInfo<StreamInfoItem> {

    /**
     * Mixes are handled as particular playlists in NewPipeExtractor. {@link PlaylistType#NORMAL} is
     * for non-mixes, while other values are for the different types of mixes. The type of a mix
     * depends on how its contents are autogenerated.
     */
    public enum PlaylistType {
        /**
         * A normal playlist (not a mix)
         */
        NORMAL,

        /**
         * A mix made only of streams related to a particular stream, for example YouTube mixes
         */
        MIX_STREAM,

        /**
         * A mix made only of music streams related to a particular stream, for example YouTube
         * music mixes
         */
        MIX_MUSIC,

        /**
         * A mix made only of streams from (or related to) the same channel, for example YouTube
         * channel mixes
         */
        MIX_CHANNEL,

        /**
         * A mix made only of streams related to a particular (musical) genre, for example YouTube
         * genre mixes
         */
        MIX_GENRE,
    }

    @SuppressWarnings("RedundantThrows")
    private PlaylistInfo(final int serviceId, final ListLinkHandler linkHandler, final String name)
            throws ParsingException {
        super(serviceId, linkHandler, name);
    }

    public static PlaylistInfo getInfo(final String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static PlaylistInfo getInfo(final StreamingService service, final String url)
            throws IOException, ExtractionException {
        final PlaylistExtractor extractor = service.getPlaylistExtractor(url);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    public static InfoItemsPage<StreamInfoItem> getMoreItems(final StreamingService service,
                                                             final String url,
                                                             final Page page)
            throws IOException, ExtractionException {
        return service.getPlaylistExtractor(url).getPage(page);
    }

    /**
     * Get PlaylistInfo from PlaylistExtractor
     *
     * @param extractor an extractor where fetchPage() was already got called on.
     */
    public static PlaylistInfo getInfo(final PlaylistExtractor extractor, boolean shouldFetchPage)
            throws ExtractionException {

        final PlaylistInfo info = new PlaylistInfo(
                extractor.getServiceId(),
                extractor.getLinkHandler(),
                extractor.getName());
        // collect uploader extraction failures until we are sure this is not
        // just a playlist without an uploader
        final List<Throwable> uploaderParsingErrors = new ArrayList<>();

        try {
            info.setOriginalUrl(extractor.getOriginalUrl());
        } catch (final Exception e) {
            info.addError(e);
        }
        try {
            info.setStreamCount(extractor.getStreamCount());
        } catch (final Exception e) {
            info.addError(e);
        }
        try {
            info.setThumbnailUrl(extractor.getThumbnailUrl());
        } catch (final Exception e) {
            info.addError(e);
        }
        try {
            info.setUploaderUrl(extractor.getUploaderUrl());
        } catch (final Exception e) {
            info.setUploaderUrl("");
            uploaderParsingErrors.add(e);
        }
        try {
            info.setUploaderName(extractor.getUploaderName());
        } catch (final Exception e) {
            info.setUploaderName("");
            uploaderParsingErrors.add(e);
        }
        try {
            info.setUploaderAvatarUrl(extractor.getUploaderAvatarUrl());
        } catch (final Exception e) {
            info.setUploaderAvatarUrl("");
            uploaderParsingErrors.add(e);
        }
        try {
            info.setSubChannelUrl(extractor.getSubChannelUrl());
        } catch (final Exception e) {
            uploaderParsingErrors.add(e);
        }
        try {
            info.setSubChannelName(extractor.getSubChannelName());
        } catch (final Exception e) {
            uploaderParsingErrors.add(e);
        }
        try {
            info.setSubChannelAvatarUrl(extractor.getSubChannelAvatarUrl());
        } catch (final Exception e) {
            uploaderParsingErrors.add(e);
        }
        try {
            info.setBannerUrl(extractor.getBannerUrl());
        } catch (final Exception e) {
            info.addError(e);
        }
        try {
            info.setPlaylistType(extractor.getPlaylistType());
        } catch (final Exception e) {
            info.addError(e);
        }

        // do not fail if everything but the uploader infos could be collected (TODO better comment)
        if (!uploaderParsingErrors.isEmpty()
                && (!info.getErrors().isEmpty() || uploaderParsingErrors.size() < 3)) {
            info.addAllErrors(uploaderParsingErrors);
        }

        if(shouldFetchPage){
            final InfoItemsPage<StreamInfoItem> itemsPage
                    = ExtractorHelper.getItemsPageOrLogError(info, extractor);
            info.setRelatedItems(itemsPage.getItems());
            info.setNextPage(itemsPage.getNextPage());
        }
        return info;
    }

    public static PlaylistInfo getInfo(final PlaylistExtractor extractor)
            throws ExtractionException {
        return getInfo(extractor, true);
    }

    public static PlaylistInfo getInfoWithFullItems(final StreamingService service, final String url)
            throws ExtractionException, IOException {
        final PlaylistExtractor extractor = service.getPlaylistExtractor(url);
        extractor.fetchPage();
        final PlaylistInfo info = getInfo(extractor);
        if (info.getServiceId() == ServiceList.YouTube.getServiceId()
                && (YoutubeParsingHelper.isYoutubeMixId(info.getId())
                || YoutubeParsingHelper.isYoutubeMusicMixId(info.getId()))){
            // YouTube mixes are infinite playlists, so we just fetch the first page
            final InfoItemsPage<StreamInfoItem> itemsPage
                    = ExtractorHelper.getItemsPageOrLogError(info, extractor);
            info.setRelatedItems(itemsPage.getItems());
            info.setNextPage(itemsPage.getNextPage());
        } else {
            InfoItemsPage<StreamInfoItem> itemsPage = ExtractorHelper.getItemsFullPageOrLogError(info, extractor);
            info.setRelatedItems(itemsPage.getItems());
            info.setNextPage(null);
        }
        return info;
    }

    private String thumbnailUrl;
    private String bannerUrl;
    private String uploaderUrl;
    private String uploaderName;
    private String uploaderAvatarUrl;
    private String subChannelUrl;
    private String subChannelName;
    private String subChannelAvatarUrl;
    private long streamCount = 0;
    private PlaylistType playlistType;

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(final String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(final String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public void setUploaderUrl(final String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(final String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderAvatarUrl() {
        return uploaderAvatarUrl;
    }

    public void setUploaderAvatarUrl(final String uploaderAvatarUrl) {
        this.uploaderAvatarUrl = uploaderAvatarUrl;
    }

    public String getSubChannelUrl() {
        return subChannelUrl;
    }

    public void setSubChannelUrl(final String subChannelUrl) {
        this.subChannelUrl = subChannelUrl;
    }

    public String getSubChannelName() {
        return subChannelName;
    }

    public void setSubChannelName(final String subChannelName) {
        this.subChannelName = subChannelName;
    }

    public String getSubChannelAvatarUrl() {
        return subChannelAvatarUrl;
    }

    public void setSubChannelAvatarUrl(final String subChannelAvatarUrl) {
        this.subChannelAvatarUrl = subChannelAvatarUrl;
    }

    public long getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(final long streamCount) {
        this.streamCount = streamCount;
    }

    public PlaylistType getPlaylistType() {
        return playlistType;
    }

    public void setPlaylistType(final PlaylistType playlistType) {
        this.playlistType = playlistType;
    }
}
