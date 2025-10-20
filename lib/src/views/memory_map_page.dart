import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:geolocator/geolocator.dart';
import '../models/memory.dart';
import '../services/location_service.dart';
import '../services/memory_storage_service.dart';

class MemoryMapPage extends StatefulWidget {
  const MemoryMapPage({super.key});

  @override
  State<MemoryMapPage> createState() => _MemoryMapPageState();
}

class _MemoryMapPageState extends State<MemoryMapPage> {
  GoogleMapController? _mapController;
  final LocationService _locationService = LocationService();
  final MemoryStorageService _storageService = MemoryStorageService();
  Position? _currentPosition;
  List<Memory> _memories = [];
  Set<Marker> _markers = {};

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    final position = await _locationService.getCurrentLocation();
    final memories = await _storageService.loadMemories();
    
    if (mounted) {
      setState(() {
        _currentPosition = position;
        _memories = memories;
        _updateMarkers();
      });
    }
  }

  void _updateMarkers() {
    final markers = <Marker>{};
    
    // Add current position marker
    if (_currentPosition != null) {
      markers.add(
        Marker(
          markerId: const MarkerId('current_location'),
          position: LatLng(
            _currentPosition!.latitude,
            _currentPosition!.longitude,
          ),
          icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueBlue),
          infoWindow: const InfoWindow(title: 'موقعك الحالي'),
        ),
      );
    }
    
    // Add memory markers
    for (final memory in _memories) {
      markers.add(
        Marker(
          markerId: MarkerId(memory.id),
          position: LatLng(memory.latitude, memory.longitude),
          icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueRed),
          infoWindow: InfoWindow(
            title: 'ذكرى',
            snippet: memory.content,
          ),
          onTap: () => _showMemoryDialog(memory),
        ),
      );
    }
    
    setState(() {
      _markers = markers;
    });
  }

  void _showMemoryDialog(Memory memory) {
    showDialog(
      context: context,
      builder: (context) => Directionality(
        textDirection: TextDirection.rtl,
        child: AlertDialog(
          title: const Text('ذكرى'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                memory.content,
                style: const TextStyle(fontSize: 16),
              ),
              const SizedBox(height: 12),
              Text(
                'التاريخ: ${memory.timestamp.toString().split('.')[0]}',
                style: const TextStyle(fontSize: 12, color: Colors.grey),
              ),
              Text(
                'الموقع: ${memory.latitude.toStringAsFixed(4)}, ${memory.longitude.toStringAsFixed(4)}',
                style: const TextStyle(fontSize: 12, color: Colors.grey),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('إغلاق'),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('خريطة الذكريات'),
        backgroundColor: Colors.blue,
        foregroundColor: Colors.white,
      ),
      body: _currentPosition == null
          ? const Center(child: CircularProgressIndicator())
          : GoogleMap(
              initialCameraPosition: CameraPosition(
                target: LatLng(
                  _currentPosition!.latitude,
                  _currentPosition!.longitude,
                ),
                zoom: 15,
              ),
              markers: _markers,
              myLocationEnabled: true,
              myLocationButtonEnabled: true,
              onMapCreated: (controller) {
                _mapController = controller;
              },
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          await _loadData();
          if (_currentPosition != null && _mapController != null) {
            _mapController!.animateCamera(
              CameraUpdate.newLatLng(
                LatLng(
                  _currentPosition!.latitude,
                  _currentPosition!.longitude,
                ),
              ),
            );
          }
        },
        child: const Icon(Icons.refresh),
      ),
    );
  }
}
