import 'dart:convert';
import 'dart:math' show cos, sin, sqrt, asin, pi;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/memory.dart';

class MemoryStorageService {
  static const String _memoriesKey = 'ar_memories';

  Future<List<Memory>> loadMemories() async {
    final prefs = await SharedPreferences.getInstance();
    final String? memoriesJson = prefs.getString(_memoriesKey);
    
    if (memoriesJson == null) {
      return [];
    }
    
    final List<dynamic> decodedList = jsonDecode(memoriesJson);
    return decodedList.map((json) => Memory.fromJson(json)).toList();
  }

  Future<void> saveMemory(Memory memory) async {
    final memories = await loadMemories();
    memories.add(memory);
    await _saveMemories(memories);
  }

  Future<void> _saveMemories(List<Memory> memories) async {
    final prefs = await SharedPreferences.getInstance();
    final String memoriesJson = jsonEncode(
      memories.map((m) => m.toJson()).toList(),
    );
    await prefs.setString(_memoriesKey, memoriesJson);
  }

  Future<List<Memory>> getNearbyMemories(
    double latitude,
    double longitude,
    double radiusInMeters,
  ) async {
    final memories = await loadMemories();
    return memories.where((memory) {
      final distance = _calculateDistance(
        latitude,
        longitude,
        memory.latitude,
        memory.longitude,
      );
      return distance <= radiusInMeters;
    }).toList();
  }

  double _calculateDistance(
    double lat1,
    double lon1,
    double lat2,
    double lon2,
  ) {
    const double earthRadius = 6371000; // meters
    final dLat = _toRadians(lat2 - lat1);
    final dLon = _toRadians(lon2 - lon1);
    
    final a = sin(dLat / 2) * sin(dLat / 2) +
        cos(_toRadians(lat1)) * cos(_toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2);
    
    final c = 2 * asin(sqrt(a));
    
    return earthRadius * c;
  }

  double _toRadians(double degrees) => degrees * pi / 180.0;
}
