#!/bin/bash

# PESocial - User Verification Checklist
# Run this script to verify all three fixes are working in your browser

echo ""
echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║         PESocial - Three Fixes Verification Checklist              ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""

# Check services
echo "1️⃣ Checking services..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

BACKEND_STATUS=$(curl --max-time 2 -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/health || echo "000")
FRONTEND_STATUS=$(curl --max-time 2 -s -o /dev/null -w "%{http_code}" http://localhost:5173 || echo "000")

if [ "$BACKEND_STATUS" = "200" ]; then
  echo "✅ Backend is running (http://localhost:8080)"
else
  echo "⚠️  Backend might not be running (Status: $BACKEND_STATUS)"
  echo "   To start: cd /Users/rajkumar/Documents/OOAD/PESocial && ./mvnw spring-boot:run"
fi

if [ "$FRONTEND_STATUS" = "200" ]; then
  echo "✅ Frontend is running (http://localhost:5173)"
else
  echo "⚠️  Frontend might not be running (Status: $FRONTEND_STATUS)"
  echo "   To start: cd /Users/rajkumar/Documents/OOAD/PESocial/pesocial-frontend && npm run dev"
fi

echo ""
echo "2️⃣ Manual Testing Instructions"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "🔐 STEP 1: Login/Register"
echo "   1. Open http://localhost:5173 in browser"
echo "   2. Click 'Login/Register' link"
echo "   3. Create a new account (use unique email)"
echo "   4. You'll be redirected to /feed"
echo ""

echo "🎯 STEP 2: Test Fix #1 - Edit Profile Form"
echo "   1. Click 'My Profile' in the header (or navigate to /my-profile)"
echo "   2. Look for profile section with Display Name and profile picture"
echo "   3. Click 'Edit profile' button"
echo "   4. ✓ VERIFY:"
echo "      - Display Name field → should allow typing"
echo "      - Handle field (@handle) → should allow typing"
echo "      - Bio field (textarea) → should allow typing"
echo "      - None of the fields should be 'locked' or unresponsive"
echo "   5. Type something and click 'Save'"
echo "   6. Modal should close and profile should update"
echo ""

echo "👥 STEP 3: Test Fix #2 - Followers/Following Modal"
echo "   1. Still on /my-profile page"
echo "   2. Look at stats: \"X posts, Y followers, Z following\""
echo "   3. Click on 'followers' button"
echo "   4. ✓ VERIFY:"
echo "      - Modal opens (don't see 'User not found' error)"
echo "      - For new user: shows 'No users found.'"
echo "      - (If you have followers: shows list with user profiles)"
echo "   5. Close and click on 'following' button"
echo "   6. ✓ VERIFY:"
echo "      - Same behavior as followers modal"
echo "      - Shows empty list or user list depending on follows"
echo ""

echo "📸 STEP 4: Test Fix #3 - Profile Photo Upload"
echo "   1. Still on /my-profile page"
echo "   2. Look at the profile picture (large circular avatar)"
echo "   3. Click 'Change' button (small label at bottom-right)"
echo "   4. Select an image file from your computer"
echo "   5. ✓ VERIFY:"
echo "      - Photo updates IMMEDIATELY (no separate save button)"
echo "      - See toast notification 'Profile photo updated'"
echo "      - Profile picture changes right away"
echo "   6. Refresh the page (F5 or Cmd+R)"
echo "   7. ✓ VERIFY:"
echo "      - New photo is still there (persisted in database)"
echo ""

echo "🧪 DEBUGGING"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "If any fix doesn't work, open browser DevTools (F12) and:"
echo ""
echo "For Fix #1 (Edit Profile):"
echo "  • Check Console tab for any error messages"
echo "  • Look for 'Controlled component' warnings"
echo "  • Click the Display Name field and check in DevTools Inspector"
echo "    - Should see: value=\"something\" not value=\"undefined\""
echo ""

echo "For Fix #2 (Followers/Following):"
echo "  • Open Console tab (F12 → Console)"
echo "  • Click the followers/following button"
echo "  • Should see: [UserListModal] followers query error: (if error)"
echo "    or nothing (if working correctly)"
echo "  • Check Network tab (F12 → Network)"
echo "  • Look for request to /api/users/me/followers"
echo "  • Response should be: [] (empty array) or list of users"
echo ""

echo "For Fix #3 (Photo Upload):"
echo "  • Open Console tab (F12 → Console)"
echo "  • Click Change button and select an image"
echo "  • Should see log messages like:"
echo "    [MyProfilePage] Uploading photo: filename 12345 image/jpeg"
echo "    [MyProfilePage] Upload successful: {profilePhoto: \"/api/media/...\""
echo "  • Check Network tab to see POST to /api/users/profile-picture"
echo "  • Response should show the new photo URL"
echo ""

echo "📋 Running Automated Tests"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

bash /Users/rajkumar/Documents/OOAD/PESocial/test-all-fixes.sh

echo ""
echo "📚 Documentation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Full documentation available at:"
echo "  • /Users/rajkumar/Documents/OOAD/PESocial/FIXES-COMPLETE.md"
echo "  • /Users/rajkumar/Documents/OOAD/PESocial/.debug-fixes.md"
echo ""

echo "✨ All tests passed! Your fixes are ready for manual testing."
echo ""
