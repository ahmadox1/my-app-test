import 'package:flutter/material.dart';

import '../models/meal_style.dart';

class MealStyleSelector extends StatelessWidget {
  const MealStyleSelector({
    super.key,
    required this.value,
    required this.onChanged,
  });

  final MealStyle value;
  final ValueChanged<MealStyle> onChanged;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: MealStyle.values.map((style) {
        final isSelected = value == style;
        return Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4),
            child: ChoiceChip(
              label: Text(style.label),
              selected: isSelected,
              onSelected: (_) => onChanged(style),
              selectedColor: const Color(0xFF1ED1A6),
              backgroundColor: const Color(0xFF0C3F38),
              labelStyle: TextStyle(
                color: isSelected ? const Color(0xFF06312B) : const Color(0xFFD3E8DF),
                fontWeight: FontWeight.w600,
              ),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(18),
                side: BorderSide(
                  color: isSelected
                      ? const Color(0xFF1ED1A6)
                      : Colors.white.withOpacity(0.08),
                ),
              ),
            ),
          ),
        );
      }).toList(),
    );
  }
}
