# GitHub Actions Workflows

This directory contains GitHub Actions workflows for the OSS Shopping App project.

## 📁 Workflows

### 🚀 `release.yml` - Automatic Release Build

**Trigger:** When a tag starting with 'v' is pushed (e.g., v1.0.0)

**What it does:**

- Builds release APK automatically
- Creates a GitHub release with the APK attached
- Includes detailed release notes
- Uploads APK as artifact

**Usage:**

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 🔄 `build-on-pr.yml` - Pull Request Build

**Trigger:** On pull requests to main branch and pushes to main

**What it does:**

- Runs tests
- Builds both debug and release APKs
- Uploads APKs as artifacts
- Comments on PR with build status

### 🎯 `manual-build.yml` - Manual Build

**Trigger:** Manual dispatch from GitHub Actions tab

**What it does:**

- Allows you to choose build type (debug/release/both)
- Optional version name input
- Builds selected APK types
- Uploads as artifacts with timestamp

**Usage:**

1. Go to Actions tab on GitHub
2. Select "Manual APK Build"
3. Click "Run workflow"
4. Choose options and run

## 📋 Issue Templates

### 🐛 Bug Report (`ISSUE_TEMPLATE/bug_report.md`)

Template for reporting bugs with device info and reproduction steps.

### ✨ Feature Request (`ISSUE_TEMPLATE/feature_request.md`)

Template for requesting new features with priority levels.

## 📝 Pull Request Template

Standardized PR template with checklists for:

- Type of change
- Testing requirements
- Code review checklist
- Related issues

## 🎯 How to Use

### For Releases:

1. **Create and push a tag:**

   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0 - Initial release"
   git push origin v1.0.0
   ```

2. **The workflow will:**
   - Build release APK
   - Create GitHub release
   - Attach APK file
   - Generate release notes

### For Testing:

1. **Create a PR** - APK will be built automatically
2. **Manual build** - Use Actions tab for on-demand builds
3. **Download artifacts** from the workflow run

### For Contributors:

1. Use issue templates for bugs/features
2. Follow PR template checklist
3. Ensure builds pass before merging

## 🔧 Configuration

All workflows use latest versions:

- **JDK 11** (Temurin distribution)
- **actions/checkout@v4**
- **actions/setup-java@v4**
- **actions/cache@v4** for Gradle caching
- **actions/upload-artifact@v4** for APK uploads
- **actions/github-script@v7** for PR comments
- **softprops/action-gh-release@v2** for releases
- **Artifact retention** (7-30 days)
- **Automatic permissions** via GITHUB_TOKEN

## 📱 APK Download

After successful builds:

1. Go to the workflow run
2. Scroll to "Artifacts" section
3. Download the APK file
4. Install on Android device (enable unknown sources)

## 🆕 Recent Updates

- ✅ Updated to `actions/upload-artifact@v4` (latest version)
- ✅ Updated to `actions/cache@v4` for better performance
- ✅ Updated to `actions/github-script@v7` for PR comments
- ✅ Updated to `softprops/action-gh-release@v2` for releases
- ✅ All deprecated actions have been replaced

## 🚨 Important Notes

- All workflows now use the latest action versions
- No more deprecation warnings
- Improved performance with updated caching
- Better artifact handling with v4 upload action
