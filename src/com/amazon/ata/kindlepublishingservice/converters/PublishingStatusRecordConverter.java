package com.amazon.ata.kindlepublishingservice.converters;

import com.amazon.ata.coral.converter.CoralConverterUtil;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

import java.util.List;

public class PublishingStatusRecordConverter {

    private PublishingStatusRecordConverter() {}


    public static List<PublishingStatusRecord> toCoral(List<PublishingStatusItem> statusItems) {
        return CoralConverterUtil.convertList(statusItems, PublishingStatusRecordConverter::toCoral);
    }



    public static PublishingStatusRecord toCoral(PublishingStatusItem statusItem) {
        return PublishingStatusRecord.builder()
                .withStatus(statusItem.getStatus().toString())
                .withStatusMessage(statusItem.getStatusMessage())
                .withBookId(statusItem.getBookId())
                .build();
    }



}
