import 'package:flutter_test/flutter_test.dart';
import 'package:ar_memory_app/src/models/memory.dart';

void main() {
  group('Memory Model Tests', () {
    test('Memory should be created correctly', () {
      final memory = Memory(
        id: 'test-id',
        content: 'Test memory content',
        latitude: 24.7136,
        longitude: 46.6753,
        timestamp: DateTime(2024, 1, 1),
      );

      expect(memory.id, 'test-id');
      expect(memory.content, 'Test memory content');
      expect(memory.latitude, 24.7136);
      expect(memory.longitude, 46.6753);
      expect(memory.userId, 'local_user');
    });

    test('Memory should convert to JSON correctly', () {
      final memory = Memory(
        id: 'test-id',
        content: 'Test content',
        latitude: 24.7136,
        longitude: 46.6753,
        timestamp: DateTime(2024, 1, 1),
      );

      final json = memory.toJson();

      expect(json['id'], 'test-id');
      expect(json['content'], 'Test content');
      expect(json['latitude'], 24.7136);
      expect(json['longitude'], 46.6753);
      expect(json['userId'], 'local_user');
    });

    test('Memory should be created from JSON correctly', () {
      final json = {
        'id': 'test-id',
        'content': 'Test content',
        'latitude': 24.7136,
        'longitude': 46.6753,
        'timestamp': '2024-01-01T00:00:00.000',
        'userId': 'local_user',
      };

      final memory = Memory.fromJson(json);

      expect(memory.id, 'test-id');
      expect(memory.content, 'Test content');
      expect(memory.latitude, 24.7136);
      expect(memory.longitude, 46.6753);
      expect(memory.userId, 'local_user');
    });
  });
}
