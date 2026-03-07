#!/usr/bin/env bash
# setup.sh — Bootstrap System Design Interview Prep environment

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILLS_DIR="$HOME/.claude/skills"
SKILL_FILE="$SCRIPT_DIR/skills/sysdesigninterviewprep/SKILL.md"

# ── 1. Install Claude Code CLI ────────────────────────────────────────────────
info "Checking for Claude Code CLI ..."

if command -v claude &>/dev/null; then
  info "Claude Code CLI already installed: $(claude --version 2>/dev/null || echo 'version unknown')"
else
  info "Installing Claude Code CLI via npm ..."
  if ! command -v npm &>/dev/null; then
    error "npm not found. Please install Node.js (https://nodejs.org) and re-run this script."
  fi
  npm install -g @anthropic-ai/claude-code
  info "Claude Code CLI installed successfully."
fi

# ── 2. IntelliJ IDEA — Claude Code plugin ────────────────────────────────────
info "IntelliJ IDEA setup ..."

# Locate the most recent IntelliJ plugins directory (macOS)
INTELLIJ_PLUGINS_DIR=$(ls -dt "$HOME/Library/Application Support/JetBrains/IntelliJIdea"*/plugins 2>/dev/null | head -1 || true)

if [[ -n "$INTELLIJ_PLUGINS_DIR" ]]; then
  info "IntelliJ plugins directory found: $INTELLIJ_PLUGINS_DIR"
  warn "Automatic plugin installation is not supported by IntelliJ's CLI."
  warn "Please install the Claude Code plugin manually:"
  echo "  1. Open IntelliJ IDEA"
  echo "  2. Go to Settings > Plugins > Marketplace"
  echo "  3. Search for 'Claude Code' and click Install"
  echo "  4. Restart IntelliJ IDEA"
else
  warn "IntelliJ IDEA installation not detected on this machine."
  warn "After installing IntelliJ, add the Claude Code plugin via:"
  echo "  Settings > Plugins > Marketplace > search 'Claude Code'"
fi

# ── 3. Copy SKILL.md ──────────────────────────────────────────────────────────
info "Copying SKILL.md to $SKILLS_DIR ..."

if [[ ! -f "$SKILL_FILE" ]]; then
  error "SKILL.md not found at $SKILL_FILE"
fi

mkdir -p "$SKILLS_DIR"
cp "$SKILL_FILE" "$SKILLS_DIR/SKILL.md"
info "SKILL.md copied successfully."

# ── Done ──────────────────────────────────────────────────────────────────────
echo ""
info "Setup complete. Next steps:"
echo "  1. Open this project in IntelliJ IDEA"
echo "  2. In the Claude Code prompt, type: /sysdesigninterviewprep"
echo "  3. Start your mock interview!"