import json
import re
import sys
from dataclasses import dataclass, asdict


@dataclass
class Message:
    attachments: list
    content: str
    username: str


@dataclass
class Image:
    url: str
    prompt: str


def process_messages(input_file, output_file_name):
    messages = []

    with open(input_file) as file:
        raw_messages = json.load(file)

        for raw_message in raw_messages:
            message = Message(
                attachments=raw_message['attachments'],
                content=raw_message['content'],
                username=raw_message['author']['username']
            )

            messages.append(message)

    messages = filter(lambda m: m.username == 'Midjourney Bot', messages)
    messages_with_image = filter(lambda m: re.search(r'.* - Image #\d', m.content), messages)

    images = [asdict(Image(
        url=m.attachments[0]['url'],
        prompt=re.search(r'\*\*([^*]+)\*', m.content).group(1) if re.search(r'\*\*([^*]+)\*', m.content) else '',
    )) for m in messages_with_image]

    with open(f'json/{output_file_name}', 'w') as file:
        json.dump(images, file, indent=4)

    print(f"Processing {len(list(messages))} messages.")
    print(f"Found {len(list(messages_with_image))} messages w/ images")


if len(sys.argv) != 3:
    print(f"Usage: python {sys.argv[0]} [input_file] [output_file_name]")
    sys.exit(1)

process_messages(*sys.argv[1:])
