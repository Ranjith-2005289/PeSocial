#!/bin/bash

# PESocial - Quick Validation Test Script
# Tests all three fixes: Edit Profile, Followers/Following, Photo Upload

set -e

BACKEND_URL="http://localhost:8080"
TIMEOUT=5

echo "🚀 PESocial Fix Validation"
echo "=========================="
echo ""

# Test 1: Backend Health
echo "📋 Test 1: Backend Availability"
STATUS=$(curl --max-time $TIMEOUT -s -o /dev/null -w "%{http_code}" "$BACKEND_URL/api/health" || echo "000")
if [ "$STATUS" = "200" ]; then
  echo "✅ Backend is running"
else
  echo "❌ Backend returned status: $STATUS"
  echo "   Start backend with: cd /Users/rajkumar/Documents/OOAD/PESocial && ./mvnw spring-boot:run"
  exit 1
fi

# Test 2: Registration
echo ""
echo "📋 Test 2: User Registration"
EMAIL="test_$(date +%s)@example.com"
RESP=$(curl -s -X POST "$BACKEND_URL/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"Test User\",\"handle\":\"@testvalid\",\"email\":\"$EMAIL\",\"password\":\"Password@123\"}")

TOKEN=$(echo "$RESP" | python3 -c 'import sys,json; print(json.load(sys.stdin).get("accessToken",""))' 2>/dev/null || echo "")

if [ -n "$TOKEN" ] && [ ${#TOKEN} -gt 100 ]; then
  echo "✅ User registration successful"
  echo "   Token length: ${#TOKEN} chars"
else
  echo "❌ Registration failed"
  echo "   Response: $RESP"
  exit 1
fi

# Test 3: Get My Profile
echo ""
echo "📋 Test 3: Get My Profile (/api/users/me)"
ME_RESP=$(curl -s "$BACKEND_URL/api/users/me" -H "Authorization: Bearer $TOKEN")
HANDLE=$(echo "$ME_RESP" | python3 -c 'import sys,json; print(json.load(sys.stdin).get("handle",""))' 2>/dev/null || echo "")

if [ "$HANDLE" = "@testvalid" ]; then
  echo "✅ Profile retrieved successfully"
  echo "   Handle: $HANDLE"
else
  echo "❌ Profile retrieval failed"
  echo "   Response: ${ME_RESP:0:100}"
  exit 1
fi

# Test 4: Get Followers (should be empty array)
echo ""
echo "📋 Test 4: Get My Followers (should be [])"
FOLLOWERS=$(curl -s "$BACKEND_URL/api/users/me/followers" -H "Authorization: Bearer $TOKEN")

if [ "$FOLLOWERS" = "[]" ]; then
  echo "✅ Followers endpoint returns empty array correctly"
else
  echo "❌ Followers endpoint returned unexpected response"
  echo "   Response: $FOLLOWERS"
  exit 1
fi

# Test 5: Get Following (should be empty array)
echo ""
echo "📋 Test 5: Get My Following (should be [])"
FOLLOWING=$(curl -s "$BACKEND_URL/api/users/me/following" -H "Authorization: Bearer $TOKEN")

if [ "$FOLLOWING" = "[]" ]; then
  echo "✅ Following endpoint returns empty array correctly"
else
  echo "❌ Following endpoint returned unexpected response"
  echo "   Response: $FOLLOWING"
  exit 1
fi

# Test 6: Photo Upload
echo ""
echo "📋 Test 6: Profile Photo Upload"

# Create a minimal PNG file (1x1 pixel)
PNG_DATA="iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
echo "$PNG_DATA" | base64 -d > /tmp/test-profile.png

UPLOAD_RESP=$(curl -s -X POST "$BACKEND_URL/api/users/profile-picture" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/tmp/test-profile.png")

PHOTO_URL=$(echo "$UPLOAD_RESP" | python3 -c 'import sys,json; print(json.load(sys.stdin).get("profilePhoto",""))' 2>/dev/null || echo "")

if [[ "$PHOTO_URL" == /api/media/* ]]; then
  echo "✅ Photo uploaded successfully"
  echo "   Photo URL: $PHOTO_URL"
else
  echo "❌ Photo upload failed"
  echo "   Response: $UPLOAD_RESP"
  exit 1
fi

# Summary
echo ""
echo "=========================="
echo "✅ All Tests Passed!"
echo "=========================="
echo ""
echo "🎯 Next Steps:"
echo "1. Visit http://localhost:5173"
echo "2. Click 'Login/Register'"
echo "3. Create a new account or use email: $EMAIL"
echo "4. Navigate to /my-profile"
echo "5. Test the three fixes:"
echo "   - Click 'Edit profile' and verify you can type in all fields"
echo "   - Click '0 followers' or '0 following' to see the modal"
echo "   - Click 'Change' on profile picture to test photo upload"
echo ""
echo "📝 For debugging, check browser DevTools Console (F12) for [UserListModal] and [MyProfilePage] logs"
