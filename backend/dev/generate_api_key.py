import secrets

def generate_urlsafe_api_key(length=32):
    """
    Generates a cryptographically strong random URL-safe text API key.
    The length refers to the number of random bytes. The resulting string
    will be longer due to base64 encoding.
    """
    return secrets.token_urlsafe(length)

api_key = generate_urlsafe_api_key(32) # Generates a URL-safe string from 32 random bytes
print(f"Generated API Key (URL-safe): {api_key}")