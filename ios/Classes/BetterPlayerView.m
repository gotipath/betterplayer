#import "BetterPlayerView.h"

@implementation BetterPlayerView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        // Ad container view setup
        _adContainerView = [[UIView alloc] initWithFrame:self.bounds];
        _adContainerView.backgroundColor = [UIColor clearColor];
        _adContainerView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        [self addSubview:_adContainerView];
    }
    return self;
}

- (AVPlayer *)player {
    return self.playerLayer.player;
}

- (void)setPlayer:(AVPlayer *)player {
    self.playerLayer.player = player;
}

+ (Class)layerClass {
    return [AVPlayerLayer class];
}

- (AVPlayerLayer *)playerLayer {
    return (AVPlayerLayer *) self.layer;
}

- (void)didMoveToWindow {
    [super didMoveToWindow];
    if (self.window && self.delegate && [self.delegate respondsToSelector:@selector(playerViewDidMoveToWindow)]) {
        [self.delegate playerViewDidMoveToWindow];
    }
}

@end