import 'package:flutter/material.dart';

import '../models/health_profile.dart';

class HealthDataCard extends StatelessWidget {
  const HealthDataCard({
    super.key,
    required this.expanded,
    required this.onToggle,
    required this.weightController,
    required this.heightController,
    required this.ageController,
    required this.goal,
    required this.onGoalChanged,
  });

  final bool expanded;
  final VoidCallback onToggle;
  final TextEditingController weightController;
  final TextEditingController heightController;
  final TextEditingController ageController;
  final HealthGoal? goal;
  final ValueChanged<HealthGoal?> onGoalChanged;

  @override
  Widget build(BuildContext context) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 250),
      curve: Curves.easeInOut,
      decoration: BoxDecoration(
        color: const Color(0xFF0C3F38).withOpacity(0.8),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.white.withOpacity(0.05)),
      ),
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                expanded ? Icons.shield_moon : Icons.shield_outlined,
                color: Colors.white.withOpacity(0.9),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  expanded ? 'إخفاء حقول البيانات' : 'إدخال البيانات الصحية',
                  style: const TextStyle(
                    fontWeight: FontWeight.w700,
                    fontSize: 16,
                  ),
                ),
              ),
              IconButton(
                onPressed: onToggle,
                icon: Icon(expanded ? Icons.keyboard_arrow_up : Icons.keyboard_arrow_down),
                color: Colors.white,
              ),
            ],
          ),
          if (expanded) ...[
            const SizedBox(height: 16),
            _HealthField(
              label: 'الوزن (كجم)',
              controller: weightController,
              inputType: TextInputType.number,
            ),
            const SizedBox(height: 12),
            _HealthField(
              label: 'الطول (سم)',
              controller: heightController,
              inputType: TextInputType.number,
            ),
            const SizedBox(height: 12),
            _HealthField(
              label: 'العمر',
              controller: ageController,
              inputType: TextInputType.number,
            ),
            const SizedBox(height: 12),
            DecoratedBox(
              decoration: BoxDecoration(
                color: const Color(0xFF0A342F),
                borderRadius: BorderRadius.circular(16),
              ),
              child: DropdownButtonHideUnderline(
                child: DropdownButton<HealthGoal>(
                  value: goal,
                  hint: const Padding(
                    padding: EdgeInsets.symmetric(horizontal: 16),
                    child: Text('الهدف'),
                  ),
                  icon: const Padding(
                    padding: EdgeInsetsDirectional.only(end: 16),
                    child: Icon(Icons.keyboard_arrow_down, color: Colors.white70),
                  ),
                  dropdownColor: const Color(0xFF0A342F),
                  borderRadius: BorderRadius.circular(16),
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w600),
                  items: HealthGoal.values
                      .map(
                        (goal) => DropdownMenuItem(
                          value: goal,
                          child: Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 16),
                            child: Text(goal.label),
                          ),
                        ),
                      )
                      .toList(),
                  onChanged: onGoalChanged,
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _HealthField extends StatelessWidget {
  const _HealthField({
    required this.label,
    required this.controller,
    required this.inputType,
  });

  final String label;
  final TextEditingController controller;
  final TextInputType inputType;

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      keyboardType: inputType,
      textDirection: TextDirection.rtl,
      decoration: InputDecoration(
        labelText: label,
      ),
    );
  }
}
