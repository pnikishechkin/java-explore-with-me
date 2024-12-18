package ru.practicum.ewm.main.compilation;

import ru.practicum.ewm.main.compilation.dto.CompilationDto;
import ru.practicum.ewm.main.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.main.compilation.dto.UpdateCompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto create(NewCompilationDto newCompilationDto);

    void delete(Long compId);

    CompilationDto update(UpdateCompilationDto updateCompilationDto);

    CompilationDto getById(Long compId);

    List<CompilationDto> get(Boolean pinned, Integer from, Integer size);
}
