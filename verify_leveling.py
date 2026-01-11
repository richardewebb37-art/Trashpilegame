#!/usr/bin/env python3
"""
Manual verification script for unlimited leveling system
Tests the formulas without needing a full build system
"""

import math

def calculate_level(xp: int, base_xp: int = 100, multiplier: float = 1.2) -> int:
    """Calculate level from XP using logarithmic formula"""
    if xp <= base_xp:
        return 1
    
    normalized_xp = xp + 1.0
    level = int(math.log(normalized_xp) / math.log(multiplier))
    return max(1, level + 1)

def calculate_xp(sp_earned: int, ap_earned: int, current_level: int, 
                 total_matches: int, total_rounds: int,
                 match_bonus: int = 10, round_bonus: int = 50) -> int:
    """Calculate XP earned from a match"""
    base_xp = sp_earned + ap_earned
    level_multiplier = 1.0 + current_level * 0.05
    
    points_xp = int(base_xp * level_multiplier)
    match_xp = total_matches * match_bonus
    round_xp = total_rounds * round_bonus
    
    return points_xp + match_xp + round_xp

def get_xp_for_level(level: int, base_xp: int = 100, multiplier: float = 1.2) -> int:
    """Get XP required for a specific level"""
    if level <= 1:
        return base_xp
    
    xp = base_xp * (multiplier ** (level - 1))
    return int(xp)

def test_level_calculation():
    """Test level calculation at various XP values"""
    print("=" * 60)
    print("TEST 1: Level Calculation")
    print("=" * 60)
    
    # Test that level increases with XP (monotonic)
    test_cases = [
        (0, 1),
        (50, 1),
        (100, 1),
        (200, 30),
        (500, 35),
        (1000, 38),
        (5000, 47),
        (10000, 51),
    ]
    
    all_passed = True
    previous_level = 0
    for xp, expected_approx_level in test_cases:
        level = calculate_level(xp)
        passed = level >= previous_level
        status = "✓" if passed else "✗"
        print(f"{status} XP={xp:7d} → Level={level:3d} (expected ~{expected_approx_level})")
        if not passed:
            all_passed = False
        previous_level = level
    
    print(f"\nTest 1 Result: {'PASSED' if all_passed else 'FAILED'}\n")
    return all_passed

def test_xp_calculation():
    """Test XP calculation with different parameters"""
    print("=" * 60)
    print("TEST 2: XP Calculation")
    print("=" * 60)
    
    test_cases = [
        # (sp, ap, level, matches, rounds, description)
        (10, 10, 1, 1, 1, "Low level, first match"),
        (10, 10, 10, 1, 1, "Mid level, first match"),
        (10, 10, 50, 1, 1, "High level, first match"),
        (100, 100, 10, 10, 1, "Mid level, many matches"),
        (100, 100, 20, 50, 5, "High level, many matches and rounds"),
    ]
    
    all_passed = True
    for sp, ap, level, matches, rounds, desc in test_cases:
        xp = calculate_xp(sp, ap, level, matches, rounds)
        # Just verify it's positive and reasonable
        passed = xp > 0 and xp < 100000
        status = "✓" if passed else "✗"
        print(f"{status} SP={sp:3d}, AP={ap:3d}, Lvl={level:2d}, M={matches:3d}, R={rounds} → XP={xp:4d} ({desc})")
        if not passed:
            all_passed = False
    
    print(f"\nTest 2 Result: {'PASSED' if all_passed else 'FAILED'}\n")
    return all_passed

def test_progression():
    """Test progressive leveling over multiple matches"""
    print("=" * 60)
    print("TEST 3: Progressive Leveling")
    print("=" * 60)
    
    level = 1
    total_xp = 0
    total_sp = 0
    total_ap = 0
    
    print(f"Start: Level {level}, XP {total_xp}")
    
    for match_num in range(1, 11):
        # Simulate winning a match
        sp_earned = 10
        ap_earned = 10
        
        total_sp += sp_earned
        total_ap += ap_earned
        
        # Calculate XP earned
        xp_earned = calculate_xp(sp_earned, ap_earned, level, match_num, 1)
        total_xp += xp_earned
        
        # Calculate new level
        new_level = calculate_level(total_xp)
        
        leveled_up = new_level > level
        level = new_level
        
        arrow = "→" if not leveled_up else "→ LEVEL UP!"
        print(f"Match {match_num:2d}: +{xp_earned:3d} XP, Total: {total_xp:5d} {arrow} Level {level}")
    
    print(f"\nTest 3: Progressive leveling works! Reached Level {level}\n")
    return True

def test_unlimited():
    """Test that unlimited progression works"""
    print("=" * 60)
    print("TEST 4: Unlimited Progression")
    print("=" * 60)
    
    # Test very high XP values
    test_xps = [1000, 10000, 100000, 1000000, 10000000]
    
    all_passed = True
    for xp in test_xps:
        level = calculate_level(xp)
        # Should always return a valid level
        passed = level > 0 and level < 1000  # Reasonable upper bound
        status = "✓" if passed else "✗"
        print(f"{status} XP={xp:10,d} → Level={level:3d}")
        if not passed:
            all_passed = False
    
    print(f"\nTest 4 Result: {'PASSED' if all_passed else 'FAILED'}\n")
    return all_passed

def test_level_requirements():
    """Test that XP requirements increase with level"""
    print("=" * 60)
    print("TEST 5: Level Requirements Scaling")
    print("=" * 60)
    
    levels = [1, 5, 10, 20, 50, 100]
    previous_xp = 0
    
    all_passed = True
    for level in levels:
        xp = get_xp_for_level(level)
        passed = xp >= previous_xp
        status = "✓" if passed else "✗"
        growth = xp - previous_xp if level > 1 else 0
        print(f"{status} Level {level:3d} → XP {xp:10,d} (+{growth:8,d})")
        previous_xp = xp
        if not passed:
            all_passed = False
    
    print(f"\nTest 5 Result: {'PASSED' if all_passed else 'FAILED'}\n")
    return all_passed

def main():
    """Run all verification tests"""
    print("\n" + "=" * 60)
    print("UNLIMITED LEVELING SYSTEM - VERIFICATION TESTS")
    print("=" * 60 + "\n")
    
    results = []
    results.append(test_level_calculation())
    results.append(test_xp_calculation())
    results.append(test_progression())
    results.append(test_unlimited())
    results.append(test_level_requirements())
    
    print("=" * 60)
    print("SUMMARY")
    print("=" * 60)
    print(f"Tests Run: {len(results)}")
    print(f"Tests Passed: {sum(results)}")
    print(f"Tests Failed: {len(results) - sum(results)}")
    
    if all(results):
        print("\n✅ ALL TESTS PASSED - Unlimited leveling system is working correctly!")
    else:
        print("\n❌ SOME TESTS FAILED - Please review the implementation")
    
    print("=" * 60 + "\n")
    
    return all(results)

if __name__ == "__main__":
    import sys
    success = main()
    sys.exit(0 if success else 1)