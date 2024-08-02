import requests
from datetime import datetime, timedelta, timezone
import json

def fetch_data():
    url = "###"
    response = requests.get(url)
    response.raise_for_status()
    return response.json()

def process_data(data):
    customer_calls = {}
    call_records = data.get("callRecords", [])

    for record in call_records:
        customer_id = record["customerId"]
        call_id = record["callId"]
        start_time = datetime.fromtimestamp(record['startTimestamp'] / 1000, tz=timezone.utc)
        end_time = datetime.fromtimestamp(record['endTimestamp'] / 1000, tz=timezone.utc)
        
        # Process each date the call spans
        current_date = start_time.date()
        end_date = end_time.date()
        
        while current_date <= end_date:
            if customer_id not in customer_calls:
                customer_calls[customer_id] = {}
            if current_date not in customer_calls[customer_id]:
                customer_calls[customer_id][current_date] = []
            
            start_of_day = datetime.combine(current_date, datetime.min.time(), tzinfo=timezone.utc)
            end_of_day = datetime.combine(current_date, datetime.max.time(), tzinfo=timezone.utc)
            
            current_start_time = max(start_time, start_of_day)
            current_end_time = min(end_time, end_of_day)
            
            if current_start_time < current_end_time:
                customer_calls[customer_id][current_date].append((current_start_time, current_end_time, call_id))
            
            current_date += timedelta(days=1)
    
    return customer_calls


def calculate_peak_loads(customer_calls):
    results = []
    used_timestamps = set()

    for customer_id, calls_by_date in customer_calls.items():
        for date, calls in calls_by_date.items():
            events = []

            
            for start_time, end_time, call_id in calls: # Create events for each call's start and end
                
                events.append((start_time, 1, call_id)) # Add event for the start of the call
               
                exclusive_end_time = end_time  # Add event for the end of the call (exclusive)
                events.append((exclusive_end_time, -1, call_id))

            
            events.sort(key=lambda x: (x[0], x[1])) # Sort events first by timestamp, then by type (end before start if same timestamp)

            current_concurrent = 0
            max_concurrent = 0
            max_timestamp = None
            max_call_ids = []
            active_call_ids = []

            # Process sorted events
            for timestamp, change, call_id in events:
                if change == 1:
                    current_concurrent += 1
                    active_call_ids.append(call_id)
                else:
                    if call_id in active_call_ids:
                        current_concurrent -= 1
                        active_call_ids.remove(call_id)

                # Update max_concurrent and corresponding timestamp and call_ids
                if current_concurrent > max_concurrent:
                    max_concurrent = current_concurrent
                    max_timestamp = timestamp
                    max_call_ids = active_call_ids.copy()

            if max_timestamp is not None:
            
                adjusted_timestamp = int(max_timestamp.timestamp() * 1000)
                while adjusted_timestamp in used_timestamps:
                    adjusted_timestamp += 1  # Increment by 1 millisecond
                used_timestamps.add(adjusted_timestamp)

                if all(adjusted_timestamp < int(datetime.timestamp(end_time) * 1000) for (start_time, end_time, call_id) in calls if call_id in max_call_ids):
                    results.append({
                        "customerId": customer_id,
                        "date": date.isoformat(),  # Ensures date is in ISO format
                        "maxConcurrentCalls": max_concurrent,
                        "timestamp": adjusted_timestamp,  # Use the unique timestamp
                        "callIds": max_call_ids
                    })

    return results


def post_results(results):
    url = "###"
    payload = {"results": results}
    headers = {'Content-Type': 'application/json'}
    response = requests.post(url, data=json.dumps(payload), headers=headers)
    return response.status_code, response.text

def save_to_json(data, filename):
    with open(filename, 'w') as file:
        json.dump(data, file, indent=4)

def save_to_txt(data, filename):
    with open(filename, 'w') as file:
        file.write(str(data))

def main():
    data = fetch_data()
    customer_calls = process_data(data)
    save_to_txt(customer_calls, 'cc.txt')

    
    results = calculate_peak_loads(customer_calls)
    save_to_json(results, 'peak_loads_results.json')

    status_code, response_text = post_results(results)
    print(f"Response Status Code: {status_code}")
    print(f"Response Text: {response_text}")

if __name__ == "__main__":
    main()
