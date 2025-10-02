import 'package:flutter/material.dart';

class IllustrationHeader extends StatelessWidget {
  const IllustrationHeader({super.key});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 180,
      child: CustomPaint(
        painter: _KitchenPainter(
          color: Theme.of(context).colorScheme.secondary,
        ),
      ),
    );
  }
}

class _KitchenPainter extends CustomPainter {
  _KitchenPainter({required this.color});

  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final bowlPaint = Paint()
      ..color = color.withOpacity(0.85)
      ..style = PaintingStyle.fill;
    final bowlRect = Rect.fromCenter(
      center: Offset(size.width * 0.5, size.height * 0.65),
      width: size.width * 0.6,
      height: size.height * 0.28,
    );
    final bowlPath = Path()
      ..moveTo(bowlRect.left, bowlRect.top)
      ..quadraticBezierTo(
        size.width * 0.5,
        size.height * 0.95,
        bowlRect.right,
        bowlRect.top,
      )
      ..quadraticBezierTo(
        size.width * 0.5,
        bowlRect.bottom + 8,
        bowlRect.left,
        bowlRect.top,
      );
    canvas.drawPath(bowlPath, bowlPaint);

    final rimPaint = Paint()
      ..color = Colors.white.withOpacity(0.9)
      ..strokeWidth = 6
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;
    final rimPath = Path()
      ..moveTo(bowlRect.left + 8, bowlRect.top)
      ..quadraticBezierTo(
        size.width * 0.5,
        size.height * 0.58,
        bowlRect.right - 8,
        bowlRect.top,
      );
    canvas.drawPath(rimPath, rimPaint);

    final whiskPaint = Paint()
      ..color = Colors.white
      ..strokeWidth = 5
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;
    canvas.drawLine(
      Offset(size.width * 0.22, size.height * 0.18),
      Offset(size.width * 0.35, size.height * 0.48),
      whiskPaint,
    );
    final whiskHandle = Paint()
      ..color = const Color(0xFFFC9F5B)
      ..strokeWidth = 10
      ..strokeCap = StrokeCap.round;
    canvas.drawLine(
      Offset(size.width * 0.18, size.height * 0.12),
      Offset(size.width * 0.22, size.height * 0.18),
      whiskHandle,
    );

    final bottlePaint = Paint()
      ..color = Colors.white.withOpacity(0.92)
      ..style = PaintingStyle.fill;
    final bottleRect = Rect.fromCenter(
      center: Offset(size.width * 0.78, size.height * 0.42),
      width: size.width * 0.08,
      height: size.height * 0.42,
    );
    final bottlePath = Path()
      ..moveTo(bottleRect.left, bottleRect.bottom)
      ..quadraticBezierTo(
        bottleRect.left - 6,
        bottleRect.center.dy,
        bottleRect.left + 6,
        bottleRect.top,
      )
      ..lineTo(bottleRect.right - 6, bottleRect.top)
      ..quadraticBezierTo(
        bottleRect.right + 6,
        bottleRect.center.dy,
        bottleRect.right,
        bottleRect.bottom,
      )
      ..close();
    canvas.drawPath(bottlePath, bottlePaint);

    final tablePaint = Paint()
      ..color = Colors.white.withOpacity(0.12)
      ..style = PaintingStyle.fill;
    canvas.drawRRect(
      RRect.fromRectAndRadius(
        Rect.fromCenter(
          center: Offset(size.width * 0.5, size.height * 0.85),
          width: size.width * 0.85,
          height: size.height * 0.12,
        ),
        const Radius.circular(18),
      ),
      tablePaint,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
