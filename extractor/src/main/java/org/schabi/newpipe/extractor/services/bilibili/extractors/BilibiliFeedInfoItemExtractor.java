package org.schabi.newpipe.extractor.services.bilibili.extractors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

public class BilibiliFeedInfoItemExtractor implements StreamInfoItemExtractor{

    protected final JsonObject item;
    public BilibiliFeedInfoItemExtractor(final JsonObject json) {
        item = json;
    }

    @Override
    public String getName() throws ParsingException {
        return item.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return item.getString("uri");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return item.getString("pic").replace("http:", "https:");
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public long getDuration() throws ParsingException {
        return item.getInt("duration");
    }

    @Override
    public long getViewCount() throws ParsingException {
        return item.getObject("stat").getInt("view");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return item.getObject("owner").getString("name");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return null;
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return item.getObject("owner").getString("face");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(item.getInt("pubdate") * 1000L));
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return null;
    }

}
