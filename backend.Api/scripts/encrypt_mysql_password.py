import sys
from getpass import getpass
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT_DIR))

from cryptography.fernet import Fernet  # noqa: E402
from core.config import settings  # noqa: E402


def main() -> None:
    password = sys.argv[1] if len(sys.argv) > 1 else getpass("MySQL password: ")
    key_path = ROOT_DIR / settings.mysql_password_key_file
    encrypted_path = ROOT_DIR / settings.mysql_password_file

    key_path.parent.mkdir(parents=True, exist_ok=True)
    encrypted_path.parent.mkdir(parents=True, exist_ok=True)

    key = Fernet.generate_key()
    encrypted_password = Fernet(key).encrypt(password.encode("utf-8"))

    key_path.write_bytes(key)
    encrypted_path.write_bytes(encrypted_password)

    print(f"Encrypted MySQL password written to {encrypted_path}")
    print(f"Encryption key written to {key_path}")


if __name__ == "__main__":
    main()
