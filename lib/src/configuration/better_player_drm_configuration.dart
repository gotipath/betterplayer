import 'package:better_player/src/configuration/better_player_drm_type.dart';

///Configuration of DRM used to protect data source
class BetterPlayerDrmConfiguration {
  ///Type of DRM
  final BetterPlayerDrmType? drmType;

  ///Parameter used only for token encrypted DRMs
  final String? token;

  ///Url of license server
  final String? licenseUrl;

  ///Url of fairplay certificate
  ///Or, Base64 Encoded Certificate File
  final String? certificateUrlOrBase64;

  ///ClearKey json object, used only for ClearKey protection. Only support for Android.
  final String? clearKey;

  ///Additional headers send with auth request, used only for WIDEVINE DRM
  final Map<String, String>? headers;

  BetterPlayerDrmConfiguration(
      {this.drmType,
      this.token,
      this.licenseUrl,
      this.certificateUrlOrBase64,
      this.headers,
      this.clearKey});
}

class DrmInfo {
  final String vendor;
  final String? version;
  final String description;
  final String algorithms;
  final String securityLevel;
  final String? maxHdcpLevel;

  DrmInfo({
    required this.vendor,
    required this.description,
    required this.algorithms,
    required this.securityLevel,
    this.version,
    this.maxHdcpLevel,
  });

  factory DrmInfo.fromMap(Map<String, dynamic> map) {
    return DrmInfo(
      vendor: map['vendor'] ?? '',
      version: map['version'],
      description: map['description'] ?? '',
      algorithms: map['algorithms'] ?? '',
      securityLevel: map['securityLevel'] ?? '',
      maxHdcpLevel: map['maxHdcpLevel'],
    );
  }
}
