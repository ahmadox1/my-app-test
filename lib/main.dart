import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:google_fonts/google_fonts.dart';

import 'src/features/recipe/views/recipe_home_page.dart';

void main() {
  runApp(const RecipeChefApp());
}

class RecipeChefApp extends StatelessWidget {
  const RecipeChefApp({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = ThemeData(
      useMaterial3: true,
      colorScheme: const ColorScheme.dark(
        primary: Color(0xFF1ED1A6),
        secondary: Color(0xFF8EF7CD),
        surface: Color(0xFF0B3B35),
        background: Color(0xFF052925),
      ),
      scaffoldBackgroundColor: const Color(0xFF052925),
      textTheme: GoogleFonts.tajawalTextTheme(
        ThemeData.dark().textTheme,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: const Color(0xFF0C3F38).withOpacity(0.8),
        hintStyle: const TextStyle(color: Color(0xFFB2C9C1)),
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 18),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide.none,
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: const Color(0xFF1ED1A6),
          foregroundColor: const Color(0xFF06312B),
          padding: const EdgeInsets.symmetric(vertical: 18),
          textStyle: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(18),
          ),
        ),
      ),
    );

    return MaterialApp(
      title: 'الطاهي الذكي',
      debugShowCheckedModeBanner: false,
      locale: const Locale('ar'),
      supportedLocales: const [Locale('ar'), Locale('en')],
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      theme: theme,
      home: const RecipeHomePage(),
    );
  }
}
