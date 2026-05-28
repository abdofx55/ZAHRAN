import sqlite3
import json
import re
import os
import sys

def normalize_name(name):
    if not name:
        return ""
    # Replace multiple spaces with a single space and strip
    return re.sub(r'\s+', ' ', name).strip()

def upload_to_firestore(records):
    print("\nStarting Firestore upload to project 'zahran-94274'...")
    try:
        import firebase_admin
        from firebase_admin import credentials
        from firebase_admin import firestore
    except ImportError:
        print("Error: firebase-admin package is not installed. Please run 'pip install firebase-admin' in terminal first.")
        return

    try:
        # Initialize Firebase App using Application Default Credentials (ADC)
        cred = credentials.ApplicationDefault()
        firebase_admin.initialize_app(cred, {
            'projectId': 'zahran-94274',
        })
        db = firestore.client()
        print("Firebase Admin SDK initialized successfully with local credentials (ADC).")
        
        # Batch write documents to 'family' collection
        # Firestore batches are capped at 500 operations. We commit every 400.
        batch = db.batch()
        count = 0
        total_uploaded = 0
        
        for person in records:
            doc_ref = db.collection('family').document(person['id'])
            batch.set(doc_ref, person)
            count += 1
            
            if count == 400:
                batch.commit()
                total_uploaded += count
                print(f"Committed batch of {count} documents (Total: {total_uploaded})...")
                batch = db.batch()
                count = 0
                
        if count > 0:
            batch.commit()
            total_uploaded += count
            print(f"Committed final batch of {count} documents (Total: {total_uploaded}).")
            
        print("Firestore database migration completed successfully!")
    except Exception as e:
        print(f"\nError uploading to Firestore: {e}")
        print("Make sure you run 'gcloud auth application-default login' in terminal to log in to your Google Account.")

def migrate_database():
    db_path = "zahran_family_tree.db"
    if not os.path.exists(db_path):
        print(f"Error: {db_path} not found in current directory.")
        sys.exit(1)
        
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    # Read all rows from old family table
    cursor.execute("SELECT id, gen, father, name, nick_name, gender, is_family FROM family;")
    rows = cursor.fetchall()
    
    print(f"Successfully loaded {len(rows)} records from SQLite.")
    
    # First pass: Build a mapping of normalized (name + " " + father) -> SQLite ID
    name_to_id_map = {}
    for r in rows:
        pid, gen, father, name, nick_name, gender, is_family = r
        full_name = normalize_name(f"{name} {father}")
        # Map full name to the person's unique SQLite ID
        name_to_id_map[full_name] = str(pid)
        
    # Second pass: Resolve parents and construct the migrated JSON objects
    migrated_records = []
    resolved_count = 0
    orphan_records = []
    
    for r in rows:
        pid, gen, father, name, nick_name, gender, is_family = r
        normalized_name_val = normalize_name(name)
        normalized_father_val = normalize_name(father)
        
        # Determine parent ID based on generation rules
        parent_id = "root"
        if gen == 1:
            # Gen 1 is the absolute root (Zahran)
            parent_id = "root"
            resolved_count += 1
        elif gen == 2 and normalized_father_val == "زهران":
            # Gen 2 children with father "زهران" map to the Gen 1 root node (which has ID 1)
            parent_id = "1"
            resolved_count += 1
        else:
            # Standard name matching for other generations
            if normalized_father_val in name_to_id_map:
                parent_id = name_to_id_map[normalized_father_val]
                resolved_count += 1
            else:
                orphan_records.append(r)
                
        # Map values to the new schema
        migrated_person = {
            "id": str(pid),
            "parentId": parent_id,
            "name": normalized_name_val,
            "fullName": normalize_name(f"{name} {father}"),
            "nickName": normalize_name(nick_name) if nick_name else "",
            "gen": int(gen),
            "gender": int(gender),
            "isDocumented": bool(is_family)
        }
        migrated_records.append(migrated_person)
        
    print(f"Resolution Summary:")
    print(f"  Total records processed: {len(rows)}")
    print(f"  Parent relationships resolved: {resolved_count}")
    print(f"  Orphans identified: {len(orphan_records)}")
    
    if orphan_records:
        print("Warning: Orphans found!")
        for o in orphan_records:
            print(f"  ID: {o[0]}, Gen: {o[1]}, Name: {o[3]}, Father: {o[2]}")
            
    # Output to local JSON file for review
    output_json_path = "zahran_family_tree_migrated.json"
    with open(output_json_path, "w", encoding="utf-8") as json_file:
        json.dump(migrated_records, json_file, ensure_ascii=False, indent=2)
    print(f"Saved local copy to {output_json_path}")
    
    # Upload to Firestore
    upload_to_firestore(migrated_records)
    
    conn.close()

if __name__ == "__main__":
    migrate_database()
