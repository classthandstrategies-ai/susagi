FROM python:3.11-slim

ARG AASIST_COMMIT=a04c9863f63d44471dde8a6abcb3b082b07cd1d1
ARG AASIST_CHECKPOINT_GIT_SHA1=edbce171dbae13ae448f210e9fd62a0b861b5713

RUN apt-get update \
    && apt-get install -y --no-install-recommends git ca-certificates libsndfile1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

RUN git clone https://github.com/clovaai/aasist.git /opt/aasist \
    && cd /opt/aasist \
    && git checkout "${AASIST_COMMIT}" \
    && test "$(git rev-parse HEAD)" = "${AASIST_COMMIT}" \
    && test "$(git hash-object models/weights/AASIST.pth)" = "${AASIST_CHECKPOINT_GIT_SHA1}" \
    && rm -rf /opt/aasist/.git

ENV AASIST_REPO_DIR=/opt/aasist
ENV AASIST_CHECKPOINT_PATH=/opt/aasist/models/weights/AASIST.pth
ENV AASIST_DEVICE=auto

COPY aasist_detector.py server.py ./
EXPOSE 8090
CMD ["gunicorn", "--bind", "0.0.0.0:8090", "--workers", "1", "--threads", "2", "--timeout", "60", "server:app"]
