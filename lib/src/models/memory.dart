class Memory {
  final String id;
  final String content;
  final double latitude;
  final double longitude;
  final DateTime timestamp;
  final String userId;

  Memory({
    required this.id,
    required this.content,
    required this.latitude,
    required this.longitude,
    required this.timestamp,
    this.userId = 'local_user',
  });

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'content': content,
      'latitude': latitude,
      'longitude': longitude,
      'timestamp': timestamp.toIso8601String(),
      'userId': userId,
    };
  }

  factory Memory.fromJson(Map<String, dynamic> json) {
    return Memory(
      id: json['id'] as String,
      content: json['content'] as String,
      latitude: json['latitude'] as double,
      longitude: json['longitude'] as double,
      timestamp: DateTime.parse(json['timestamp'] as String),
      userId: json['userId'] as String? ?? 'local_user',
    );
  }
}
