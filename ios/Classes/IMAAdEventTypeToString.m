#import <Foundation/Foundation.h>
#import <GoogleInteractiveMediaAds/IMAAdEvent.h>

NSString *IMAAdEventTypeToString(IMAAdEventType type);

NSString *IMAAdEventTypeToString(IMAAdEventType type) {
    switch (type) {
        case kIMAAdEvent_AD_BREAK_READY: return @"kIMAAdEvent_AD_BREAK_READY";
        case kIMAAdEvent_AD_BREAK_FETCH_ERROR: return @"kIMAAdEvent_AD_BREAK_FETCH_ERROR";
        case kIMAAdEvent_AD_BREAK_ENDED: return @"kIMAAdEvent_AD_BREAK_ENDED";
        case kIMAAdEvent_AD_BREAK_STARTED: return @"kIMAAdEvent_AD_BREAK_STARTED";
        case kIMAAdEvent_AD_PERIOD_ENDED: return @"kIMAAdEvent_AD_PERIOD_ENDED";
        case kIMAAdEvent_AD_PERIOD_STARTED: return @"kIMAAdEvent_AD_PERIOD_STARTED";
        case kIMAAdEvent_ALL_ADS_COMPLETED: return @"kIMAAdEvent_ALL_ADS_COMPLETED";
        case kIMAAdEvent_CLICKED: return @"kIMAAdEvent_CLICKED";
        case kIMAAdEvent_COMPLETE: return @"kIMAAdEvent_COMPLETE";
        case kIMAAdEvent_CUEPOINTS_CHANGED: return @"kIMAAdEvent_CUEPOINTS_CHANGED";
        case kIMAAdEvent_ICON_FALLBACK_IMAGE_CLOSED: return @"kIMAAdEvent_ICON_FALLBACK_IMAGE_CLOSED";
        case kIMAAdEvent_ICON_TAPPED: return @"kIMAAdEvent_ICON_TAPPED";
        case kIMAAdEvent_FIRST_QUARTILE: return @"kIMAAdEvent_FIRST_QUARTILE";
        case kIMAAdEvent_LOADED: return @"kIMAAdEvent_LOADED";
        case kIMAAdEvent_LOG: return @"kIMAAdEvent_LOG";
        case kIMAAdEvent_MIDPOINT: return @"kIMAAdEvent_MIDPOINT";
        case kIMAAdEvent_PAUSE: return @"kIMAAdEvent_PAUSE";
        case kIMAAdEvent_RESUME: return @"kIMAAdEvent_RESUME";
        case kIMAAdEvent_SKIPPED: return @"kIMAAdEvent_SKIPPED";
        case kIMAAdEvent_STARTED: return @"kIMAAdEvent_STARTED";
        case kIMAAdEvent_STREAM_LOADED: return @"kIMAAdEvent_STREAM_LOADED";
        case kIMAAdEvent_STREAM_STARTED: return @"kIMAAdEvent_STREAM_STARTED";
        case kIMAAdEvent_TAPPED: return @"kIMAAdEvent_TAPPED";
        case kIMAAdEvent_THIRD_QUARTILE: return @"kIMAAdEvent_THIRD_QUARTILE";
        default: return [NSString stringWithFormat:@"Unknown (%ld)", (long)type];
    }
}