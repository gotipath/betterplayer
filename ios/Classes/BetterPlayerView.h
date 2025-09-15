#import <UIKit/UIKit.h>
#import <AVKit/AVKit.h>
#import <AVFoundation/AVFoundation.h>

@protocol BetterPlayerViewDelegate <NSObject>
- (void)playerViewDidMoveToWindow;
@end

@interface BetterPlayerView : UIView
@property(nonatomic, strong) AVPlayer *player;
@property(nonatomic, readonly) AVPlayerLayer *playerLayer;
@property(nonatomic, strong) UIView *adContainerView;
@property(nonatomic, weak) id <BetterPlayerViewDelegate> delegate;
@end