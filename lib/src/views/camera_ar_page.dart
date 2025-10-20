import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:geolocator/geolocator.dart';
import '../models/memory.dart';
import '../services/location_service.dart';
import '../services/memory_storage_service.dart';
import 'memory_map_page.dart';
import 'package:uuid/uuid.dart';

class CameraARPage extends StatefulWidget {
  const CameraARPage({super.key});

  @override
  State<CameraARPage> createState() => _CameraARPageState();
}

class _CameraARPageState extends State<CameraARPage> {
  CameraController? _controller;
  List<CameraDescription>? _cameras;
  final LocationService _locationService = LocationService();
  final MemoryStorageService _storageService = MemoryStorageService();
  Position? _currentPosition;
  List<Memory> _nearbyMemories = [];
  bool _isLoading = true;
  final TextEditingController _memoryController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _initializeCamera();
    _startLocationTracking();
  }

  Future<void> _initializeCamera() async {
    try {
      _cameras = await availableCameras();
      if (_cameras != null && _cameras!.isNotEmpty) {
        _controller = CameraController(
          _cameras![0],
          ResolutionPreset.high,
          enableAudio: false,
        );
        await _controller!.initialize();
        if (mounted) {
          setState(() {
            _isLoading = false;
          });
        }
      }
    } catch (e) {
      debugPrint('Error initializing camera: $e');
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _startLocationTracking() async {
    final position = await _locationService.getCurrentLocation();
    if (position != null && mounted) {
      setState(() {
        _currentPosition = position;
      });
      _loadNearbyMemories();
    }

    _locationService.getLocationStream().listen((position) {
      if (mounted) {
        setState(() {
          _currentPosition = position;
        });
        _loadNearbyMemories();
      }
    });
  }

  Future<void> _loadNearbyMemories() async {
    if (_currentPosition == null) return;
    
    final memories = await _storageService.getNearbyMemories(
      _currentPosition!.latitude,
      _currentPosition!.longitude,
      50, // 50 meters radius
    );
    
    if (mounted) {
      setState(() {
        _nearbyMemories = memories;
      });
    }
  }

  Future<void> _saveMemory() async {
    if (_memoryController.text.isEmpty || _currentPosition == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('الرجاء كتابة ذكرى وتحديد الموقع')),
      );
      return;
    }

    final memory = Memory(
      id: const Uuid().v4(),
      content: _memoryController.text,
      latitude: _currentPosition!.latitude,
      longitude: _currentPosition!.longitude,
      timestamp: DateTime.now(),
    );

    await _storageService.saveMemory(memory);
    _memoryController.clear();
    
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('تم حفظ الذكرى بنجاح!')),
      );
      _loadNearbyMemories();
    }
  }

  @override
  void dispose() {
    _controller?.dispose();
    _memoryController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Stack(
              children: [
                // Camera Preview
                if (_controller != null && _controller!.value.isInitialized)
                  SizedBox.expand(
                    child: CameraPreview(_controller!),
                  )
                else
                  const Center(
                    child: Text(
                      'الكاميرا غير متاحة',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),

                // AR Overlay - Show nearby memories
                ..._buildARMemoryOverlays(),

                // Top Status Bar
                SafeArea(
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: Colors.black54,
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text(
                                'ذكريات AR',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 20,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                _currentPosition != null
                                    ? 'الموقع: ${_currentPosition!.latitude.toStringAsFixed(4)}, ${_currentPosition!.longitude.toStringAsFixed(4)}'
                                    : 'جاري تحديد الموقع...',
                                style: const TextStyle(
                                  color: Colors.white70,
                                  fontSize: 12,
                                ),
                              ),
                              if (_nearbyMemories.isNotEmpty)
                                Text(
                                  'ذكريات قريبة: ${_nearbyMemories.length}',
                                  style: const TextStyle(
                                    color: Colors.yellow,
                                    fontSize: 12,
                                  ),
                                ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),

                // Bottom Controls
                Positioned(
                  bottom: 0,
                  left: 0,
                  right: 0,
                  child: Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.bottomCenter,
                        end: Alignment.topCenter,
                        colors: [
                          Colors.black.withOpacity(0.8),
                          Colors.transparent,
                        ],
                      ),
                    ),
                    child: SafeArea(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          // Memory input field
                          TextField(
                            controller: _memoryController,
                            style: const TextStyle(color: Colors.white),
                            textAlign: TextAlign.right,
                            decoration: InputDecoration(
                              hintText: 'اكتب ذكرى هنا...',
                              hintStyle: const TextStyle(color: Colors.white60),
                              filled: true,
                              fillColor: Colors.black54,
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: BorderSide.none,
                              ),
                              suffixIcon: IconButton(
                                icon: const Icon(Icons.send, color: Colors.blue),
                                onPressed: _saveMemory,
                              ),
                            ),
                            maxLines: 2,
                          ),
                          const SizedBox(height: 16),
                          // Map button
                          ElevatedButton.icon(
                            onPressed: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) => const MemoryMapPage(),
                                ),
                              );
                            },
                            icon: const Icon(Icons.map),
                            label: const Text('عرض الخريطة'),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.blue,
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(
                                horizontal: 32,
                                vertical: 16,
                              ),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
    );
  }

  List<Widget> _buildARMemoryOverlays() {
    return _nearbyMemories.map((memory) {
      return Positioned(
        top: 150 + (_nearbyMemories.indexOf(memory) * 80.0),
        left: 20,
        right: 20,
        child: Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Colors.blue.withOpacity(0.7),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: Colors.white, width: 2),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Icon(Icons.location_on, color: Colors.white, size: 16),
                  const SizedBox(width: 4),
                  Text(
                    '${_calculateDistanceToMemory(memory).toStringAsFixed(0)}م',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 12,
                    ),
                  ),
                  const Spacer(),
                  Text(
                    _formatTimestamp(memory.timestamp),
                    style: const TextStyle(
                      color: Colors.white70,
                      fontSize: 10,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                memory.content,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 16,
                ),
                textAlign: TextAlign.right,
              ),
            ],
          ),
        ),
      );
    }).toList();
  }

  double _calculateDistanceToMemory(Memory memory) {
    if (_currentPosition == null) return 0;
    return _locationService.calculateDistance(
      _currentPosition!.latitude,
      _currentPosition!.longitude,
      memory.latitude,
      memory.longitude,
    );
  }

  String _formatTimestamp(DateTime timestamp) {
    final now = DateTime.now();
    final difference = now.difference(timestamp);
    
    if (difference.inMinutes < 60) {
      return 'منذ ${difference.inMinutes} دقيقة';
    } else if (difference.inHours < 24) {
      return 'منذ ${difference.inHours} ساعة';
    } else {
      return 'منذ ${difference.inDays} يوم';
    }
  }
}
