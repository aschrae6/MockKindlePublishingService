package com.amazon.ata.kindlepublishingservice.converters;

import com.amazon.ata.coral.converter.CoralConverterUtil;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

import java.util.List;

/**
 * Converters for PublishingStatus objects
 */
public class PublishingStatusRecordConverter {

    private PublishingStatusRecordConverter() {}

    /**
     * Converts the given {@link PublishingStatusItem} list into a corresponding {@link PublishingStatusRecord} list.
     *
     * @param statusItems PublishingStatusItems list to convert
     * @return Coral PublishingStatusRecords list
     */
    public static List<PublishingStatusRecord> toCoral(List<PublishingStatusItem> statusItems) {
        return CoralConverterUtil.convertList(statusItems, PublishingStatusRecordConverter::toCoral);
    }

    /**
     * Converts the given PublishingStatusItem object to the corresponding PublishingStatusRecord object.
     *
     * @param statusItem PublishingStatusItem object to convert
     * @return Converted PublishingStatusRecord result
     */
    public static PublishingStatusRecord toCoral(PublishingStatusItem statusItem) {
        return PublishingStatusRecord.builder()
                .withStatus(statusItem.getStatus().toString())
                .withStatusMessage(statusItem.getStatusMessage())
                .withBookId(statusItem.getBookId())
                .build();
    }



}
