import ast
import csv
import re
import sys
from datetime import datetime

ios_indices = {
    "error_code": ("☠️", "-"),
    "url_success": ("✅", "⏳"),
    "url_failure": ("☠️", "⏳"),
    "elapsed": ("⏳", "🌐"),
    "request": ("🌐", "⏩"),
    "response_success": "⏩",
    "response_failure": ("⏩", ";❌"),
    "message_failure": "❌"
}

url_regex = "(?:(?:https?|ftp):\/\/)?[\w/\-?=%.]+\.[\w/\-&?=%.]+"
rows = ["timestamp", "url", "status", "elapsed", "request", "response", "message"]


def parse_android(logs: str, output_file: str) -> None:
    filtered = list(filter(None, re.split("==================", logs)))
    with open(output_file, "w", newline="") as csvfile:
        writer = csv.writer(csvfile, delimiter=";")
        writer.writerow(rows)
        for item in filtered:

            def extract_response() -> str:
                response_full = item[
                                str.find(item, "---------- Response ----------") +
                                len("---------- Response ----------"):]
                brace = str.find(response_full, "{")
                arr = str.find(response_full, "[")
                if brace == -1:
                    start = arr
                    end = str.rfind(response_full, "]")
                elif arr == -1:
                    start = brace
                    end = str.rfind(response_full, "}")
                elif arr < brace:
                    start = arr
                    end = str.rfind(response_full, "}")
                else:
                    start = brace
                    end = str.rfind(response_full, "}")
                return response_full[start:end + 1]

            def extract_timestamp() -> int:
                date = item[
                       str.find(item, "Request time:") + len("Request time: "):
                       str.find(item, "Response time:") - 1]
                return int(datetime.strptime(date, "%a %b %d %H:%M:%S %Z%z %Y").timestamp())

            try:
                url = item[str.find(item, "URL:") + 5:str.find(item, "Method:") - 1]
                status_code = int(item[str.find(item, "Response:") + len("Response: "):str.find(item, "SSL:") - 1])
                elapsed = float(
                    item[str.find(item, "Duration:") + len("Duration: "):str.find(item, "Request size:") - 5]) / 1000
                request = item[
                          str.find(item, "---------- Request ----------") + len("---------- Request ----------"):
                          str.find(item, "---------- Response ----------") - 2
                          ]
                response = extract_response()

                def extract_message() -> str | None:
                    if str.find(response, """"message":""") != -1:
                        return ast.literal_eval(response)["error"]["message"]
                    else:
                        return None

                message = extract_message()
                timestamp = extract_timestamp()

                writer.writerow([timestamp, url, status_code, elapsed, request, response, message])
            except Exception:
                writer.writerow([None, None, None, None, None, None, item])


def parse_ios(logs: str, output_file: str) -> None:
    filtered = list(filter(None, re.split("([0-9]{2}.[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:)", logs)))
    with open(output_file, "w", newline="") as csvfile:
        writer = csv.writer(csvfile, delimiter=";")
        writer.writerow(rows)

        for i in range(0, len(filtered), 2):
            timestamp = int(
                datetime.strptime(filtered[i][:-1], "%d.%m %H:%M:%S")
                .replace(year=datetime.now().year)
                .timestamp()
            )

            content = filtered[i + 1]
            try:
                if "✅" not in content and "☠️" not in content:
                    writer.writerow([timestamp, None, None, None, None, None, content])
                else:
                    elapsed = float(
                        content[str.find(content, ios_indices["elapsed"][0]) + 1:
                                str.find(content, ios_indices["elapsed"][1]) - 1]
                    )
                    request = content[str.find(content, ios_indices["request"][0]) + 4:
                                      str.find(content, ios_indices["request"][0]) - 1]
                    if "✅" in content:
                        status_code = 200
                        url = re.findall(
                            url_regex,
                            content[str.find(content, ios_indices["url_success"][0]):
                                    str.find(content, ios_indices["url_success"][1])]
                        )[0][:-1]

                        response = content[str.find(content, ios_indices["response_success"]) + 2:]
                        writer.writerow([timestamp, url, status_code, elapsed, request, response, None])
                    elif "☠️" in content:
                        status_code = int(
                            content[str.find(content, ios_indices["error_code"][0]) + 2:
                                    str.find(content, ios_indices["error_code"][1]) - 1]
                        )
                        url = re.findall(
                            url_regex,
                            content[str.find(content, ios_indices["url_failure"][0]):
                                    str.find(content, ios_indices["url_failure"][1])]
                        )[0][:-1]
                        response = content[str.find(content, ios_indices["response_failure"][0]) + 2:
                                           str.find(content, ios_indices["response_failure"][1])]
                        message = content[str.find(content, ios_indices["message_failure"]) + 1:]
                        writer.writerow([timestamp, url, status_code, elapsed, request, response, message])
            except Exception:
                writer.writerow([timestamp, None, None, None, None, None, content])


if __name__ == "__main__":
    platform = sys.argv[1]
    input_file = sys.argv[2]
    output_file = sys.argv[3]
    with open(input_file) as f:
        logs = f.read()
        match platform:
            case "ios":
                parse_ios(logs, output_file)
            case "android":
                parse_android(logs, output_file)
            case _:
                pass
