enum HealthGoal {
  maintain('الحفاظ على الوزن'),
  lose('فقدان الوزن'),
  gain('زيادة الوزن');

  const HealthGoal(this.label);
  final String label;

  String get promptValue => switch (this) {
        HealthGoal.maintain => 'maintain weight',
        HealthGoal.lose => 'lose weight',
        HealthGoal.gain => 'gain weight',
      };
}

class HealthProfile {
  const HealthProfile({
    this.weightKg,
    this.heightCm,
    this.age,
    this.goal,
  });

  final double? weightKg;
  final double? heightCm;
  final int? age;
  final HealthGoal? goal;

  bool get isComplete =>
      weightKg != null && heightCm != null && age != null && goal != null;
}
