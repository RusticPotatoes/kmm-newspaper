keystore:
	openssl base64 < your_signing_keystore.jks | tr -d '\n' | tee your_signing_keystore_base64_encoded.txt